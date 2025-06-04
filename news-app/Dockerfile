# ---------- build ----------
FROM node:20 AS build
WORKDIR /client
COPY news-app/package*.json ./
RUN npm ci
COPY news-app .
RUN npm run build          # build/ готов

# ---------- runtime ----------
FROM nginx:1.25-alpine
COPY --from=build /client/build /usr/share/nginx/html

# SPA-fallback и прокси /api → ms-news:8080
RUN printf 'server {\n\
    listen 80;\n\
    server_name _;\n\
    root /usr/share/nginx/html;\n\
    location /api/ {\n\
        proxy_pass http://ms-news:8080/;\n\
    }\n\
    location / { try_files $uri /index.html; }\n\
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80                  # наружу отдаём через 3001
