# Сборка Java-приложения
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Финальный образ
FROM nginx:alpine
RUN apk add --no-cache openjdk17 supervisor
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisord.conf
COPY index.html cart.html orders.html /usr/share/nginx/html/
RUN mkdir -p /data/product_icons && chmod -R 755 /data
EXPOSE 80 8080
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
