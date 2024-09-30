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

# Второй этап: создание образа для запуска Java-приложения
FROM openjdk:11-jre-slim AS java-runtime

# Копируем jar-файл из этапа сборки
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Указываем рабочую директорию для приложения
WORKDIR /app

# Команда для запуска Java-приложения
CMD ["java", "-jar", "TelegramWebApp-1.0.0-RELEASE.jar"]

# Третий этап: настройка Nginx для статических файлов
FROM nginx:alpine AS nginx-runtime

# Копируем статические файлы в директорию Nginx
COPY index.html /usr/share/nginx/html/

# Открываем порт 80 для Nginx
EXPOSE 80

# Настраиваем Nginx на хост 0.0.0.0
CMD ["nginx", "-g", "daemon off;"]
