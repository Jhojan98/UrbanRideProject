# Etapa 1: Build
FROM node:18-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Copiar archivo de variables de entorno
# Copiar archivo de variables de entorno
COPY .env ./

# Configuración de variables de entorno para build
ARG VUE_APP_WEBSOCKET_STATIONS_URL
ENV VUE_APP_WEBSOCKET_STATIONS_URL=$VUE_APP_WEBSOCKET_STATIONS_URL

# Instalar dependencias
RUN npm install

# Copiar el código fuente
COPY . .

# Compilar la aplicación
RUN npm run build

# Etapa 2: Producción
FROM nginx:alpine

# Copiar configuración nginx personalizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar archivos compilados desde la etapa de build
COPY --from=builder /app/dist /usr/share/nginx/html

# Exponer puerto
EXPOSE 80

# Comando de inicio
CMD ["nginx", "-g", "daemon off;"]
