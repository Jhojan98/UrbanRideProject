FROM node:18-alpine AS build

WORKDIR /app

# Copiar archivos de dependencia primero para cachear la instalación
COPY package*.json ./

# Instalar dependencias (fallback a npm install si npm ci falla)
RUN npm ci --silent || npm install --silent

# Copiar el resto del código y construir la aplicación
COPY . .

# Declarar argumentos de build para variables de entorno de Firebase
ARG VUE_APP_FIREBASE_API_KEY
ARG VUE_APP_FIREBASE_AUTH_DOMAIN
ARG VUE_APP_FIREBASE_PROJECT_ID
ARG VUE_APP_FIREBASE_STORAGE_BUCKET
ARG VUE_APP_FIREBASE_MESSAGING_SENDER_ID
ARG VUE_APP_FIREBASE_APP_ID
ARG VUE_APP_FIREBASE_MEASUREMENT_ID

# Exportar las variables de entorno para el build
ENV VUE_APP_FIREBASE_API_KEY=$VUE_APP_FIREBASE_API_KEY
ENV VUE_APP_FIREBASE_AUTH_DOMAIN=$VUE_APP_FIREBASE_AUTH_DOMAIN
ENV VUE_APP_FIREBASE_PROJECT_ID=$VUE_APP_FIREBASE_PROJECT_ID
ENV VUE_APP_FIREBASE_STORAGE_BUCKET=$VUE_APP_FIREBASE_STORAGE_BUCKET
ENV VUE_APP_FIREBASE_MESSAGING_SENDER_ID=$VUE_APP_FIREBASE_MESSAGING_SENDER_ID
ENV VUE_APP_FIREBASE_APP_ID=$VUE_APP_FIREBASE_APP_ID
ENV VUE_APP_FIREBASE_MEASUREMENT_ID=$VUE_APP_FIREBASE_MEASUREMENT_ID
RUN npm run build

FROM nginx:stable-alpine

# Copiar artefactos estáticos al servidor nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración de nginx para soporte SPA (try_files)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
