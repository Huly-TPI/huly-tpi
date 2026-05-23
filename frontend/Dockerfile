# Etapa 1: build de la app
FROM node:20-alpine AS build
WORKDIR /app

# Copiar archivos de dependencias primero (mejor uso de cache de Docker)
COPY package.json package-lock.json* ./
RUN npm install

# Copiar el resto del código fuente
COPY . .

# Variable de entorno para apuntar al backend
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Buildear la app de producción
RUN npm run build

# Etapa 2: servir los archivos estáticos con nginx
FROM nginx:alpine

# Copiar los archivos buildeados desde la etapa anterior
COPY --from=build /app/dist /usr/share/nginx/html

# Configuración personalizada de nginx (para que React Router funcione)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]