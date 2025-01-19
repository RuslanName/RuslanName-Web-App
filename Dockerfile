# Сборка Java-приложения
FROM docker.io/library/maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Финальный образ
FROM nginx:alpine
RUN apk add --no-cache openjdk17 supervisor
COPY --from=build /app/target/TelegramWebApp-1.0.0-RELEASE.jar /app/
COPY --from=build /app/src/main/resources/templates/html/index.html /usr/share/nginx/html/
COPY --from=build /app/src/main/resources/templates/html/cart.html /usr/share/nginx/html/
COPY --from=build /app/src/main/resources/templates/html/order.html /usr/share/nginx/html/
COPY --from=build /app/src/main/resources/templates/html/orderHistory.html /usr/share/nginx/html/
COPY --from=build /app/src/main/resources/templates/html/orderRegistration.html /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisord.conf
RUN mkdir -p /data/product_icons && chmod -R 755 /data
EXPOSE 80 8080
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
