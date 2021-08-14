FROM 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-minecraft-dist
WORKDIR /app/minecraft-home/plugins
ADD ./plugins.tar .
RUN mkdir lib
WORKDIR /app/minecraft-home/plugins/lib
ADD ./jar-deps.tar .
WORKDIR /app/minecraft-home
ADD ./cage.json .

CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
