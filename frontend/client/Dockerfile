FROM node:18-alpine AS build

WORKDIR /app

# Copiar archivos de dependencia primero para cachear la instalación
COPY package*.json ./

# Instalar dependencias (fallback a npm install si npm ci falla)
RUN npm ci --silent || npm install --silent

# Copiar el .env desde la carpeta padre
COPY ../.env ../.env

# Copiar el resto del código y construir la aplicación
COPY . .
RUN npm run build

FROM nginx:stable-alpine

# Copiar artefactos estáticos al servidor nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración de nginx para soporte SPA (try_files)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
