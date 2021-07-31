FROM 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-minecraft-dist
WORKDIR /app/minecraft-home/plugins
ADD ./plugins.tar .
RUN mkdir lib
WORKDIR /app/minecraft-home/plugins/lib
ADD ./juneau-marshall-8.1.3.jar .
ADD ./juneau-rest-client-8.1.3.jar .
ADD ./httpcore-4.4.13.jar .
WORKDIR /app/minecraft-home/plugins
RUN mkdir -p WorldEdit/schematics
WORKDIR /app/minecraft-home/plugins/WorldEdit/schematics
ADD ./mushroomcage.schematic .
WORKDIR /app/minecraft-home

CMD ["java", "-jar", "spigot-1.8.8.jar", "nogui"]
