# Первый этап: сборка Java-приложения
FROM openjdk:11-jre-slim AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл JAR в контейнер
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Второй этап: запуск Nginx и Java-приложения
FROM nginx:alpine

# Копируем конфигурацию Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Копируем скомпилированное Java-приложение
COPY --from=builder /app/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Открываем порт 80
EXPOSE 80

# Открываем порт для вашего Java-приложения (например, 8080)
EXPOSE 8080

# Запускаем Nginx и Java-приложение
CMD ["sh", "-c", "java -jar /app/TelegramWebApp-1.0.0-RELEASE.jar & nginx -g 'daemon off;'"]

