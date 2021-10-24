#!/bin/bash
set -vx
while :
do
    $(./pick-map.rb) && rm -rf world && tar -xf world.tar.gz
    java -DmapName="${SYNDICATE_MAP_NAME}" -jar spigot-1.8.8.jar nogui
    rm -rf world # clean up
    sleep 1
done
