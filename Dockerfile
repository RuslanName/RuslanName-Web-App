# Используем официальный образ Nginx
FROM nginx:alpine

# Указываем рабочую директорию
WORKDIR /usr/share/nginx/html

# Копируем index.html в контейнер
COPY index.html .

# Открываем порт 80
EXPOSE 80

# Команда запуска по умолчанию для Nginx
CMD ["nginx", "-g", "daemon off;"]
