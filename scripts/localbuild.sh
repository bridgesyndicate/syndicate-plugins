#!/usr/bin/env bash
set -e

cd /home/harry/syndicate-plugins/
rm -rf tmp
mkdir tmp
aws s3 cp s3://syndicate-plugins-artifacts/worldedit-bukkit-6.1.9.jar tmp/
find . -type f -path \*target\* -name \*jar -not -name \*original\* -exec cp -p {} tmp/ \;
tar -C tmp -cvf ./plugins.tar .
aws s3 cp s3://syndicate-plugins-artifacts/mushroomcage.schematic .
aws s3 cp s3://syndicate-plugins-artifacts/juneau-marshall-8.1.3.jar .
aws s3 cp s3://syndicate-plugins-artifacts/juneau-rest-client-8.1.3.jar .
aws s3 cp s3://syndicate-plugins-artifacts/httpcore-4.4.13.jar .
docker build -t 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:latest .
rm plugins.tar mushroomcage.schematic juneau-*.jar httpcore-4.4.13.jar
docker run -p 25565:25565 -it 595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:latest
