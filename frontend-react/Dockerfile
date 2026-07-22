# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies (cached unless package.json changes)
COPY package*.json ./
RUN npm install

# Copy source and build the production static files
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ---- Run stage ----
FROM nginx:alpine
# Copy the built static files into nginx's web root
COPY --from=build /app/dist /usr/share/nginx/html
# Copy our custom nginx config (handles React Router)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]