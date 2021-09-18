FROM openjdk:8-jdk-slim-buster
WORKDIR /app
ADD minecraft-home.tar.xz .
WORKDIR /app/minecraft-home
CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
