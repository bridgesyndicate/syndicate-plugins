#!/bin/bash
set -evx
while :
do
    if [ -f "KYS" ]; then
      for ((n=0;n<60;n++))
      do
        echo waiting to be killed
        sleep 10
      done
      rm "KYS"
    else
      $(./pick-map.rb)
      echo picked map $SYNDICATE_MAP_NAME
      echo map uri    $SYNDICATE_MAP_URI
      echo map meta   $SYNDICATE_MAP_META
      rm -rf world && tar -xf world.tar.gz
      java -Xms768M -Xmx768M -XX:+UseG1GC -DmapName=$SYNDICATE_MAP_NAME -jar spigot-1.8.8.jar nogui --noconsole
      rm -rf world # clean up
      sleep 1
    fi
done
