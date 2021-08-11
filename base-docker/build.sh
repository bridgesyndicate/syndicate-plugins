#!/bin/bash
aws s3 cp s3://syndicate-minecraft-artifacts/minecraft-home.04.tar.xz .
REPOSITORY_HOST=595508394202.dkr.ecr.us-west-2.amazonaws.com
REPOSITORY_URI=$REPOSITORY_HOST/syn-minecraft-dist
aws ecr get-login-password  | docker login --username AWS --password-stdin $REPOSITORY_HOST
docker build -t $REPOSITORY_URI:latest .
docker push $REPOSITORY_URI:latest
