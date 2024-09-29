# Используем официальный образ Nginx
FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# Копируем конфигурацию Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Открываем порт 80
EXPOSE 80

# Настраиваем Nginx на хост 0.0.0.0
CMD ["nginx", "-g", "daemon off;"]
