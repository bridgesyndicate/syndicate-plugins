FROM 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-minecraft-dist
WORKDIR /app/minecraft-home/plugins
ADD ./plugins.tar .
RUN mkdir lib
WORKDIR /app/minecraft-home/plugins/lib
ADD ./jar-deps.tar .
WORKDIR /app/minecraft-home
ADD ./cage.json .
ADD ./run-spigot.sh .
ADD ./pick-map.rb .
ADD ./worlds-mainfest.json .
ADD ./meta.json .
RUN mkdir sample-json
ADD sample-json/game.json sample-json/game.json
CMD ["./run-spigot.sh"]
