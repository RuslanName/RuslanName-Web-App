# Сборка Java-приложения
FROM docker.io/library/maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Финальный образ
FROM openjdk:17-alpine

# Копирование JAR файла приложения
COPY --from=build /build/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp.jar

# Создание директории для данных
RUN mkdir -p /data/product_icons && chmod -R 755 /data

# Установка порта
EXPOSE 80

# Запуск Java-приложения
CMD ["java", "-jar", "/app/TelegramWebApp.jar"]

# # Сборка Java-приложения
# FROM docker.io/library/maven:3.8.5-openjdk-17 AS build
# WORKDIR /build
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package

# # Финальный образ
# FROM nginx:alpine
# RUN apk add --no-cache openjdk17 supervisor

# # Копирование JAR файла приложения
# COPY --from=build /build/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp.jar

# # Копирование HTML файлов
# COPY --from=build /build/src/main/resources/templates/html/ /usr/share/nginx/html/

# # Копирование CSS и JS файлов
# COPY --from=build /build/src/main/resources/templates/css/ /usr/share/nginx/html/css/
# COPY --from=build /build/src/main/resources/templates/js/ /usr/share/nginx/html/js/

# # Копирование конфигураций
# COPY nginx.conf /etc/nginx/nginx.conf
# COPY supervisord.ini /etc/supervisord.ini

# # Установка прав доступа
# RUN chmod -R 755 /usr/share/nginx/html
# RUN mkdir -p /data/product_icons && chmod -R 755 /data

# # Установка портов
# EXPOSE 8080 8081

# # Запуск supervisord
# CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.ini"]

