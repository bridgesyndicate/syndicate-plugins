#!/bin/bash
set -evx
while :
do
    $(./pick-map.rb)
    echo picked map $SYNDICATE_MAP_NAME
    rm -rf world && tar -xf world.tar.gz
    java -Xms1000M -Xmx1000M -XX:+UseG1GC -DmapName=$SYNDICATE_MAP_NAME -jar spigot-1.8.8.jar nogui --noconsole
    rm -rf world # clean up
    sleep 1
done
