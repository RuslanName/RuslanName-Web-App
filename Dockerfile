# # Используем официальный образ Nginx
# FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# # Открываем порт 80
# EXPOSE 80

# # Настраиваем Nginx на хост 0.0.0.0
# CMD ["nginx", "-g", "daemon off;"]

# Этап 1: Сборка Maven проекта
FROM maven:3.8.5-openjdk-11 AS build

# Копируем проект в контейнер
COPY . /app
WORKDIR /app

# Собираем Java-приложение
RUN mvn clean package

# Этап 2: Установка Nginx и Supervisor
FROM nginx:alpine

# Устанавливаем Supervisor
RUN apk add --no-cache openjdk11 supervisor

# Копируем собранный jar-файл из первого этапа
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем файл index.html в Nginx
COPY index.html /usr/share/nginx/html/index.html

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisord.conf

# Открываем порт 80 для Nginx
EXPOSE 80

# Запуск Supervisor для управления Nginx и Java-приложением
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]




