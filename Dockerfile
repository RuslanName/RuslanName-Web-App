# Сборка Java-приложения
FROM docker.io/library/maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package

# Финальный образ
FROM nginx:alpine
RUN apk add --no-cache openjdk17 supervisor

# Копирование JAR файла приложения
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/

# Копирование HTML файлов
COPY --from=build /app/src/main/resources/templates/html/ /usr/share/nginx/html/

# Копирование CSS и JS файлов
COPY --from=build /app/src/main/resources/templates/css/ /usr/share/nginx/html/css/
COPY --from=build /app/src/main/resources/templates/js/ /usr/share/nginx/html/js/

# Копирование конфигураций
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisord.conf

# Создание директории для иконок и установка прав
RUN mkdir -p /data/product_icons && chmod -R 755 /data

EXPOSE 80 8080

# Запуск supervisord
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
