# # Используем официальный образ Nginx
# FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# # Открываем порт 80
# EXPOSE 80

# # Настраиваем Nginx на хост 0.0.0.0
# CMD ["nginx", "-g", "daemon off;"]

# Используем официальный образ Nginx
FROM nginx:alpine

# Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
COPY index.html /usr/share/nginx/html/

# Устанавливаем Supervisor для управления Nginx и Java-приложением
RUN apk add --no-cache openjdk11 supervisor

# Копируем jar-файл вашего Java-приложения (бота)
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisord.conf

# Открываем порт 80 для Nginx
EXPOSE 80

# Настраиваем Supervisor на запуск Nginx и бота
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]




