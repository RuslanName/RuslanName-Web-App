# # Этап 1: Сборка Maven проекта
# FROM maven:3.8.5-openjdk-11 AS build

# # Копируем проект в контейнер
# COPY . /app
# WORKDIR /app

# # Собираем Java-приложение, пропуская тесты
# RUN mvn clean package -DskipTests

# # Этап 2: Установка Nginx и Supervisor
# FROM nginx:alpine

# # Устанавливаем Supervisor
# RUN apk add --no-cache openjdk11 supervisor

# # Копируем собранный jar-файл из первого этапа
# COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# # Копируем html файл в Nginx
# COPY index.html /usr/share/nginx/html/
# # COPY shop.html /usr/share/nginx/html/
# COPY cart.html /usr/share/nginx/html/
# COPY orders.html /usr/share/nginx/html/

# # Копируем конфигурацию для Supervisor
# COPY supervisord.conf /etc/supervisord.conf

# # Открываем порт 80 для Nginx
# EXPOSE 80

# # Запуск Supervisor для управления Nginx и Java-приложением
# CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

# # Этап 1: Сборка Maven проекта
# FROM maven:3.8.5-openjdk-11 AS build

# # Копируем проект в контейнер
# COPY . /app
# WORKDIR /app

# # Собираем Java-приложение, пропуская тесты
# RUN mvn clean package -DskipTests

# # Этап 2: Установка Nginx и Supervisor
# FROM nginx:alpine

# # Устанавливаем Java и Supervisor
# RUN apk add --no-cache openjdk11 supervisor

# # Копируем собранный jar-файл из первого этапа
# COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# # Копируем html файлы в Nginx
# COPY index.html /usr/share/nginx/html/
# COPY cart.html /usr/share/nginx/html/
# COPY orders.html /usr/share/nginx/html/

# # Копируем конфигурацию Nginx для проксирования API
# COPY nginx.conf /etc/nginx/nginx.conf

# # Копируем конфигурацию для Supervisor
# COPY supervisord.conf /etc/supervisord.conf

# # Открываем порты для Nginx и Java приложения
# EXPOSE 80
# EXPOSE 8080

# # Запуск Supervisor для управления Nginx и Java-приложением
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

# Создаем директорию для постоянного хранения данных
RUN mkdir -p /data

# Копируем html файлы в Nginx
COPY index.html /usr/share/nginx/html/
COPY cart.html /usr/share/nginx/html/
COPY orders.html /usr/share/nginx/html/

# Копируем конфигурацию Nginx для проксирования API
COPY nginx.conf /etc/nginx/nginx.conf

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisord.conf

# Открываем порты для Nginx и Java приложения
EXPOSE 80
EXPOSE 8080

# Указываем точку монтирования для постоянного хранения
VOLUME ["/data"]

# Запуск Supervisor для управления Nginx и Java-приложением
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]



