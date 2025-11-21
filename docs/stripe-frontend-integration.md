# Guía de Integración Frontend con Stripe y UrbanRide

Esta guía explica cómo el *frontend* debe integrarse con Stripe usando el **gateway** de UrbanRide para **registrar métodos de pago** mediante la interfaz oficial de Stripe (Stripe.js y Payment Element).

## 1. Flujo general

1. El usuario inicia sesión y obtiene un **JWT** (token de acceso) desde `/auth/login`.
2. El frontend llama al endpoint del gateway:
   - `POST /payments/stripe/setup-intent`
   - Incluye el JWT en el header `Authorization`.
   - El backend crea o recupera un **Stripe Customer** y un **SetupIntent**.
3. El gateway devuelve al frontend:

   ```json
   {
     "setup_intent_id": "seti_...",
     "client_secret": "seti_..._secret_...",
     "customer_id": "cus_..."
   }
   ```

4. El frontend usa `client_secret` con **Stripe.js** para renderizar el formulario de tarjeta (Payment Element) y confirmar el `SetupIntent`.
5. Stripe guarda el método de pago (`PaymentMethod`) asociado al `Customer` (`customer_id`).

> Nota: La persistencia del método de pago en la BD interna de UrbanRide vía webhook aún no está implementada en esta guía; esta parte cubre solo la integración frontend con Stripe y el uso del endpoint del gateway.

---

## 2. Endpoint backend disponible para el frontend

### 2.1. Gateway: crear SetupIntent de Stripe

**Ruta:**

```http
POST /payments/stripe/setup-intent
```

**Headers requeridos:**

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

**Body:**

- No requiere body. El gateway obtiene el `k_user_cc` del JWT.

**Respuesta de éxito (HTTP 200):**

```json
{
  "setup_intent_id": "seti_1P0X8bJ...",
  "client_secret": "seti_1P0X8bJ..._secret_abc123",
  "customer_id": "cus_PxY..."
}
```

Campos:

- `setup_intent_id`: ID del SetupIntent creado en Stripe.
- `client_secret`: **secreto que usa el frontend** para inicializar Stripe Elements.
- `customer_id`: ID del Stripe Customer asociado al usuario actual.

**Códigos de error posibles:**

- `401` → Token inválido o sin `k_user_cc`.
- `500` → Configuración incompleta del `PAYMENT_SERVICE_URL` o error interno.
- `503` → Servicio de pagos no disponible.

---

## 3. Integración en el Frontend con Stripe.js

### 3.1. Requisitos

- Tener la **publishable key** de Stripe (`pk_test_...` o `pk_live_...`) correspondiente a la cuenta configurada en backend.
- Cargar Stripe.js en el frontend:

```html
<script src="https://js.stripe.com/v3/"></script>
```

### 3.2. Flujo paso a paso en el frontend

#### Paso 1: Obtener el `client_secret` desde el gateway

Ejemplo en JavaScript (fetch API):

```js
async function createSetupIntent() {
  const response = await fetch("/payments/stripe/setup-intent", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken}`, // JWT obtenido en /auth/login
    },
    body: JSON.stringify({}),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.detail || "Error creando SetupIntent");
  }

  return response.json();
}
```

#### Paso 2: Inicializar Stripe y Stripe Elements

```js
// Publishable key de Stripe (modo test o live)
const stripe = Stripe("pk_test_XXXXXXXXXXXXXXXXXXXXXXXX");

async function initPaymentElement() {
  const { client_secret } = await createSetupIntent();

  const options = {
    clientSecret: client_secret,
  };

  const elements = stripe.elements(options);
  const paymentElement = elements.create("payment");

  // `#payment-element` es un div en tu HTML donde se insertará el formulario de Stripe
  paymentElement.mount("#payment-element");

  return { stripe, elements };
}
```

#### Paso 3: Confirmar el SetupIntent cuando el usuario envía el formulario

```js
async function handleSubmit(e) {
  e.preventDefault();

  const { stripe, elements } = await initPaymentElement();

  const { error } = await stripe.confirmSetup({
    elements,
    confirmParams: {
      return_url: "https://tu-frontend.com/stripe/success", // URL de retorno después de confirmar
    },
  });

  if (error) {
    // Mostrar el error en la UI
    console.error(error.message);
    // Ejemplo: mostrar en un <div id="payment-error">
    document.getElementById("payment-error").textContent = error.message;
  } else {
    // Stripe redirigirá automáticamente a return_url si todo sale bien
  }
}
```

HTML mínimo de ejemplo:

```html
<form id="payment-form">
  <div id="payment-element"></div>
  <div id="payment-error"></div>
  <button id="submit">Guardar método de pago</button>
</form>
```

Y enlazar el `handleSubmit`:

```js
document.getElementById("payment-form").addEventListener("submit", handleSubmit);
```

> Nota: En una SPA real (React/Vue/etc.) este flujo se implementa dentro de componentes, pero la idea es la misma.

---

## 4. Consideraciones de seguridad

- El frontend **nunca** debe enviar el número completo de tarjeta al backend; toda la captura de tarjeta se hace en el Payment Element de Stripe.
- El backend solo ve:
  - `setup_intent_id`, `client_secret`, `customer_id`.
  - Más adelante, a través de webhooks o consultas a Stripe, podrá obtener los IDs de `PaymentMethod` (`pm_...`), pero no los datos sensibles.

---

## 5. Próximos pasos (opcional, para backend)

Esta guía cubre únicamente la parte que afecta al frontend.

Para completar el ciclo de registro de un método de pago en UrbanRide, el backend puede implementar:

1. **Webhook de Stripe** (`/webhooks/stripe` en el payment-service):
   - Escuchar eventos `setup_intent.succeeded`.
   - Leer de `event.data.object`:
     - `customer` (ID del cliente en Stripe).
     - `payment_method` (ID del método de pago: `pm_...`).
     - `metadata.k_usuario_cc` para saber a qué usuario interno pertenece.

2. Con esa información, crear o actualizar un registro en la tabla interna `payment_method` o en una tabla de mapeo (p. ej. `stripe_payment_method`), de modo que el método de pago registrado vía Stripe aparezca en los endpoints `/payments/methods` del gateway.

Esto permitirá que el frontend use únicamente la UI de Stripe para registrar tarjetas, mientras que el backend mantiene su lógica de negocio y estado interno.
