FROM openjdk:8-jdk-slim-buster
WORKDIR /app
ADD minecraft-home.tar.xz .
WORKDIR /app/minecraft-home
RUN apt-get update && apt-get install -y ruby awscli
CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
