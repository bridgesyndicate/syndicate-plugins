#!/usr/bin/env bash
set -e

./scripts/localbuild.rb | grep -v 'aws ecr' | bash
# rm plugins.tar mushroomcage.schematic *.jar
docker run \
       -e AWS_REGION=us-west-2 \
       -e AWS_ACCESS_KEY_ID=$DEV_AWS_ACCESS_KEY_ID \
       -e AWS_SECRET_KEY=$DEV_AWS_SECRET_KEY \
       -e SYNDICATE_MATCH_QUEUE_NAME=syndicate-matches \
       -e ECS_CONTAINER_METADATA_URI_V4=https://kenpublic.s3.amazonaws.com/2021-08-04/sAfyVbYGjZAJlKli/container-metadata.json \
       -p 25565:25565 \
       -it \
       595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:latest
