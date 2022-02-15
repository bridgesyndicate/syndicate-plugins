#!/bin/bash
set -evx

$(./pick-map.rb)
echo picked map $SYNDICATE_MAP_NAME
rm -rf world && tar -xf world.tar.gz

while :
do
    java -Xmx1g -DmapName=$SYNDICATE_MAP_NAME -jar spigot-1.8.8.jar nogui --noconsole
    sleep 1
done
