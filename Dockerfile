# Используем Java 11 JDK
FROM openjdk:11-jdk-slim AS app

# Копируем JAR-файл вашего приложения
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Используем официальный образ Nginx
FROM nginx:alpine

# Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
COPY index.html /usr/share/nginx/html/

# Копируем конфигурацию Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Открываем порт 80
EXPOSE 80

# Открываем еще один порт для вашего Java-приложения (например, 8080)
EXPOSE 8080

# Запускаем Java-приложение в фоновом режиме и Nginx
CMD java -jar /app/TelegramWebApp-1.0.0-RELEASE.jar & nginx -g "daemon off;"
