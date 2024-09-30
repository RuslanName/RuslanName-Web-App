# # Используем официальный образ Nginx
# FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# # Открываем порт 80
# EXPOSE 80

# # Настраиваем Nginx на хост 0.0.0.0
# CMD ["nginx", "-g", "daemon off;"]

# Базовый образ для Nginx
FROM nginx:alpine AS webserver

# Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
COPY index.html /usr/share/nginx/html/

# Второй этап: установка Java и Supervisor
FROM openjdk:11-jre-slim

# Устанавливаем Nginx и Supervisor
RUN apt-get update && apt-get install -y nginx supervisor && apt-get clean

# Копируем статические файлы из первого этапа
COPY --from=webserver /usr/share/nginx/html /usr/share/nginx/html

# Копируем jar-файл вашего Java-приложения (бота)
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Открываем порт 80 для Nginx
EXPOSE 80

# Запуск Supervisor для управления Nginx и Java приложением
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

