#!/bin/bash
set -v
while :
do
    $(pick-map.rb) && rm -rf world && tar -xf world.tar.gz
    java $SYNDICATE_JAVA_OPTS -jar spigot-1.8.8.jar nogui
    rm -rf world # clean up
    sleep 1
done
