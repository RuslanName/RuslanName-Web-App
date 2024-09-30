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

# Второй этап: установка Nginx и Supervisor
FROM openjdk:11-jre-slim

# Устанавливаем Nginx и Supervisor
RUN apt-get update && apt-get install -y nginx supervisor && apt-get clean

# Копируем собранный jar-файл из первого этапа
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Копируем файл index.html
COPY index.html /usr/share/nginx/html/index.html

# Явно удаляем стандартную страницу Nginx, чтобы не было конфликта
RUN rm /usr/share/nginx/html/index.nginx-debian.html

# Копируем конфигурацию для Supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Открываем порт 80 для Nginx
EXPOSE 80

# Запуск Supervisor для управления Nginx и Java-приложением
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]



