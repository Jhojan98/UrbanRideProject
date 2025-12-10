# Etapa 1: Construcción
FROM node:16-alpine AS build

# Variables de entorno para el build
ARG VUE_APP_FIREBASE_API_KEY
ARG VUE_APP_FIREBASE_AUTH_DOMAIN
ARG VUE_APP_FIREBASE_PROJECT_ID
ARG VUE_APP_FIREBASE_STORAGE_BUCKET
ARG VUE_APP_FIREBASE_MESSAGING_SENDER_ID
ARG VUE_APP_FIREBASE_APP_ID
ARG VUE_APP_FIREBASE_MEASUREMENT_ID
ARG VUE_APP_API_URL

ENV VUE_APP_FIREBASE_API_KEY=$VUE_APP_FIREBASE_API_KEY
ENV VUE_APP_FIREBASE_AUTH_DOMAIN=$VUE_APP_FIREBASE_AUTH_DOMAIN
ENV VUE_APP_FIREBASE_PROJECT_ID=$VUE_APP_FIREBASE_PROJECT_ID
ENV VUE_APP_FIREBASE_STORAGE_BUCKET=$VUE_APP_FIREBASE_STORAGE_BUCKET
ENV VUE_APP_FIREBASE_MESSAGING_SENDER_ID=$VUE_APP_FIREBASE_MESSAGING_SENDER_ID
ENV VUE_APP_FIREBASE_APP_ID=$VUE_APP_FIREBASE_APP_ID
ENV VUE_APP_FIREBASE_MEASUREMENT_ID=$VUE_APP_FIREBASE_MEASUREMENT_ID
ENV VUE_APP_API_URL=$VUE_APP_API_URL

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración
COPY package*.json ./

# Instalar dependencias
RUN npm install

# Copiar el resto del código fuente
COPY . .

# Construir la aplicación para producción
RUN npm run build

# Etapa 2: Producción con Nginx
FROM nginx:alpine

# Copiar los archivos construidos
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar la configuración de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer el puerto 80
EXPOSE 80

# Iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]

