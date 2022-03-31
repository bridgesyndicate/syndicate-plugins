FROM openjdk:8-jdk-slim-buster
RUN apt-get update && apt-get install -y ruby awscli
WORKDIR /app
ADD minecraft-home.tar.xz .
WORKDIR /app/minecraft-home
COPY plugins-Vulcan-config.yml ./plugins/Vulcan/config.yml
CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
