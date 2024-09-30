# # Используем официальный образ Nginx
# FROM nginx:alpine

# # Копируем ваш index.html в директорию, откуда Nginx будет обслуживать файлы
# COPY index.html /usr/share/nginx/html/

# # Открываем порт 80
# EXPOSE 80

# # Настраиваем Nginx на хост 0.0.0.0
# CMD ["nginx", "-g", "daemon off;"]

# Указываем базовый образ с JDK
FROM maven:3.8.5-openjdk-11 AS build

# Копируем проект в контейнер
COPY . /app
WORKDIR /app

# Собираем приложение с помощью Maven
RUN mvn clean package

# Второй этап, базируемся на JDK образе для запуска
FROM openjdk:11-jre-slim

# Копируем jar-файл из предыдущего этапа сборки
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp-1.0.0-RELEASE.jar

# Указываем рабочую директорию
WORKDIR /app

# Определяем порт, который будет открыт в контейнере
EXPOSE 80

# Команда для запуска приложения
CMD ["java", "-jar", "TelegramWebApp-1.0.0-RELEASE.jar"]
