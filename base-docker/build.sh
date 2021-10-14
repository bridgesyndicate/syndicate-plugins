#!/bin/bash
set -ve
VERSION=09
echo version $VERSION
echo building with the contents of minecraft-home
echo edit if you don\'t have this directory
tar -cJf minecraft-home.${VERSION}.tar.xz minecraft-home/
aws s3 cp minecraft-home.${VERSION}.tar.xz s3://syndicate-minecraft-artifacts/
mv minecraft-home.${VERSION}.tar.xz minecraft-home.tar.xz
# aws s3 cp s3://syndicate-minecraft-artifacts/minecraft-home.${VERSION}.tar.xz minecraft-home.tar.xz
REPOSITORY_HOST=595508394202.dkr.ecr.us-west-2.amazonaws.com
REPOSITORY_URI=$REPOSITORY_HOST/syn-minecraft-dist
aws ecr get-login-password  | docker login --username AWS --password-stdin $REPOSITORY_HOST
docker build -t $REPOSITORY_URI:latest .
docker push $REPOSITORY_URI:latest
rm minecraft-home.tar.xz
