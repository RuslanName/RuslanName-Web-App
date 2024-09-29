# Используем базовый образ с JDK 11 для запуска Java приложения
FROM adoptopenjdk:11-jre-hotspot

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл JAR вашего приложения в контейнер
COPY target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp.jar

# Используем официальный образ Nginx
FROM nginx:alpine

# Копируем конфигурацию Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Копируем статические файлы из первого этапа сборки, если есть
COPY --from=java-build /app/TelegramWebApp.jar /app/TelegramWebApp.jar

# Открываем порт 80
EXPOSE 80

# Настраиваем Nginx на хост 0.0.0.0
CMD ["nginx", "-g", "daemon off;"]

# Запуск Java приложения
CMD ["java", "-jar", "/app/TelegramWebApp.jar"]


