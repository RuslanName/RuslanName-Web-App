# Используем официальный образ Nginx
FROM nginx:alpine

# Копируем конфигурацию Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Открываем порт 80
EXPOSE 80

# Настраиваем Nginx на хост 0.0.0.0
CMD ["nginx", "-g", "daemon off;"]
