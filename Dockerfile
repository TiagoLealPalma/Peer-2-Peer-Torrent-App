From amazoncorretto:22

WORKDIR /app

COPY target/peer-2-peer-torrent-1.0.0.jar

ENTRYPOINT ["java", "-jar", "app.jar"]