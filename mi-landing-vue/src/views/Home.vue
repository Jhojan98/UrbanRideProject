<template>
  <section class="hero">
    <div class="hero-grid">
      <div class="hero-left">
        <h2>EcoBici IoT — Bicicletas Inteligentes</h2>
        <p class="lead">
          Movilidad urbana sostenible con estaciones inteligentes. Reserva, desbloquea y regresa con seguridad.
        </p>

        <ul class="features">
          <li><strong>Estaciones conectadas</strong> con disponibilidad en tiempo real.</li>
          <li><strong>Reserva rápida</strong> desde la app en menos de 30 segundos.</li>
          <li><strong>Pagos seguros</strong> y recibos digitales.</li>
          <li><strong>Mantenimiento proactivo</strong> con detección automática de fallos.</li>
        </ul>

        <div class="cta-row">
          <button class="btn" @click="$router.push('/')">Explorar</button>
        </div>
      </div>

      <div class="hero-right">
        <div class="card">
          <h3>Cómo funciona</h3>
          <ol>
            <li>Regístrate e inicia sesión.</li>
            <li>Busca una estación cercana en el mapa.</li>
            <li>Reserva y desbloquea la bici desde la app.</li>
            <li>Finaliza el viaje y recibe el recibo.</li>
          </ol>
        </div>
      </div>
    </div>

    <section class="values">
      <h4>Nuestros valores</h4>
      <div class="grid-values">
        <div class="vcard"><h5>Sostenibilidad</h5><p>Ayudamos a reducir la congestión y las emisiones.</p></div>
        <div class="vcard"><h5>Seguridad</h5><p>Monitoreo y mantenimiento continuo para viajes seguros.</p></div>
        <div class="vcard"><h5>Confiabilidad</h5><p>Estaciones disponibles y asistencia rápida.</p></div>
      </div>
    </section>

    <section class="about">
      <h3>Sobre nosotros</h3>
      <p>
        EcoBici IoT es una empresa dedicada a ofrecer soluciones de movilidad urbana sostenible.
        Nuestra flota está equipada con sensores que permiten monitorear el estado de cada bicicleta
        y garantizar un servicio seguro y confiable. Contamos con un equipo técnico disponible 24/7
        para resolver incidencias y mantener la calidad del servicio.
      </p>

      <p><strong>Contacto:</strong> soporte@ecobici.example — <strong>Tel:</strong> +57 300 000 0000</p>
    </section>
  </section>
</template>

<script>
export default { name: 'Home' }
</script>

<style scoped>
.hero-grid { display:grid; grid-template-columns: 1fr 380px; gap:28px; align-items:start; }
.hero-left h2 { margin:0 0 6px; color: #083926; }
.lead { color: #425a52; font-size:16px; margin-bottom:18px; }
.features { list-style: none; padding:0; margin:0 0 14px 0; }
features li { padding:8px 0; border-bottom:1px dashed rgba(0,0,0,0.04); }

card { background: white; padding:16px; border-radius:10px; box-shadow: 0 8px 18px rgba(2,22,16,0.06); }
.cta-row { margin-top:12px; }

.values { margin-top:28px; }
.grid-values { display:flex; gap:12px; margin-top:12px; }
.vcard { flex:1; background:linear-gradient(180deg,#fff,#f6fffb); padding:12px; border-radius:8px; text-align:center; }

.about { margin-top:24px; background:#fff; padding:14px; border-radius:8px; box-shadow:0 6px 14px rgba(0,0,0,0.03) }
</style>