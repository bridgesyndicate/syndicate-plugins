FROM 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-minecraft-dist
WORKDIR /app/minecraft-home/plugins
ADD ./plugins.tar .
WORKDIR /app/minecraft-home
CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
