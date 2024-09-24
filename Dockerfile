# Базовый образ для Java
FROM openjdk:17-jdk-slim as java-app

# Копируем бот в контейнер
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp.jar

# Порт, на котором будет работать бот
EXPOSE 8443

# Команда для запуска бота
CMD ["java", "-jar", "/app/TelegramWebApp.jar"]

# Базовый образ для Nginx (для раздачи сайта)
FROM nginx:alpine as web-app

# Копируем сайт в директорию Nginx
COPY ./ /usr/share/nginx/html

# Копируем конфигурацию для HTTPS (замените на свой файл)
COPY ./nginx.conf /etc/nginx/nginx.conf

# Порт для HTTPS
EXPOSE 443

# Запуск Nginx
CMD ["nginx", "-g", "daemon off;"]

# Мультистейдж билд для запуска Java и Nginx
FROM web-app as final-app

COPY --from=java-app /app/TelegramWebApp.jar /app/TelegramWebApp.jar

# Сначала запустить бота, затем сайт
CMD ["sh", "-c", "java -jar /app/TelegramWebApp.jar & nginx -g 'daemon off;'"]
