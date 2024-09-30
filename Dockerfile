# # Используем официальный образ Nginx
# FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# # Открываем порт 80
# EXPOSE 80

# # Настраиваем Nginx на хост 0.0.0.0
# CMD ["nginx", "-g", "daemon off;"]

# Первый этап: сборка Maven проекта
FROM maven:3.8.5-openjdk-11 AS build

# Копируем проект в контейнер
COPY . /app
WORKDIR /app

# Собираем Java-приложение
RUN mvn clean package

# Второй этап: установка Nginx и Java Runtime
FROM openjdk:11-jre-slim

# Устанавливаем Nginx и Supervisor
RUN apt-get update && apt-get install -y nginx supervisor && apt-get clean

# Копируем jar-файл из этапа сборки
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем ваш файл index.html в директорию Nginx
COPY index.html /usr/share/nginx/html/index.html

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Указываем, что Nginx будет работать на порту 80
EXPOSE 80

# Запуск Supervisor для управления Nginx и Java
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
