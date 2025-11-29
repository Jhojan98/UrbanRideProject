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
# Usamos archivo inline generado aquí porque nginx.conf no existe aún en el contexto
RUN printf "server {\n\n    listen 80;\n    server_name localhost;\n    root /usr/share/nginx/html;\n    index index.html;\n\n    location / {\n        try_files $uri $uri/ /index.html;\n    }\n\n    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {\n        expires 1y;\n        add_header Cache-Control 'public, immutable';\n    }\n\n    location = /index.html {\n        add_header Cache-Control 'no-cache, no-store, must-revalidate';\n    }\n\n    gzip on;\n    gzip_vary on;\n    gzip_min_length 1024;\n    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/javascript application/xml+rss application/json;\n}\n" > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
