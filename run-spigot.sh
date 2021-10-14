#!/bin/bash
while :
do
    java -jar spigot-1.8.8.jar nogui
    rm -rf world
    tar -xf world-pristine.tar.gz
    sleep 1
done
