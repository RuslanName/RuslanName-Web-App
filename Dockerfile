# # Сборка Java-приложения
# FROM maven:3.8.5-openjdk-11 AS build
# WORKDIR /app
# COPY . /app
# RUN mvn clean package -DskipTests

# # Финальный образ
# FROM nginx:alpine
# RUN apk add --no-cache openjdk11 supervisor
# COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/
# COPY nginx.conf /etc/nginx/nginx.conf
# COPY supervisord.conf /etc/supervisord.conf
# COPY index.html cart.html orders.html /usr/share/nginx/html/
# RUN mkdir -p /data/product_icons && chmod -R 755 /data
# EXPOSE 80 8080
# CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

# Этап 1: Сборка Maven проекта
FROM maven:3.8.5-openjdk-11 AS build

# Копируем проект в контейнер
COPY . /app
WORKDIR /app

# Собираем Java-приложение, пропуская тесты
RUN mvn clean package -DskipTests

# Этап 2: Установка Nginx и Supervisor
FROM nginx:alpine

# Устанавливаем Java и Supervisor
RUN apk add --no-cache openjdk11 supervisor

# Копируем собранный jar-файл из первого этапа
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем html файлы в Nginx
COPY index.html /usr/share/nginx/html/
COPY cart.html /usr/share/nginx/html/
COPY orders.html /usr/share/nginx/html/

# Копируем конфигурацию Nginx для проксирования API
COPY nginx.conf /etc/nginx/nginx.conf

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisord.conf

# Создаем директорию для базы данных и изображений
RUN mkdir -p /data/product_icons && chmod -R 755 /data

# Открываем порты для Nginx и Java приложения
EXPOSE 80
EXPOSE 8080

# Запуск Supervisor для управления Nginx и Java-приложением
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]



