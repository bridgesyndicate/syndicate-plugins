#!/usr/bin/env bash
set -e

./scripts/localbuild.rb | grep -v 'aws ecr' | bash
# rm plugins.tar mushroomcage.schematic *.jar
docker run -p 25565:25565 -it 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:latest
