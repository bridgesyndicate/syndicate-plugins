#!/bin/bash
set -ve

VERSION=13
REPOSITORY_HOST=595508394202.dkr.ecr.us-west-2.amazonaws.com
REPOSITORY_URI=$REPOSITORY_HOST/syn-minecraft-dist

if [ ! -d "minecraft-home" ]; then
    echo downloading version $VERSION of minecraft home from s3
    aws s3 cp s3://syndicate-minecraft-artifacts/minecraft-home.${VERSION}.tar.xz minecraft-home.tar.xz
else
    echo building version $VERSION with the contents of minecraft-home
    tar -cJf minecraft-home.${VERSION}.tar.xz minecraft-home/
    echo sending it to the cloud
    aws s3 cp minecraft-home.${VERSION}.tar.xz s3://syndicate-minecraft-artifacts/
fi

mv minecraft-home.${VERSION}.tar.xz minecraft-home.tar.xz
aws ecr get-login-password  | docker login --username AWS --password-stdin $REPOSITORY_HOST
docker build -t $REPOSITORY_URI:latest .
docker push $REPOSITORY_URI:latest
rm minecraft-home.tar.xz
