#!/usr/bin/env bash
set -evx
TAG=latest
IMAGE=595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers
echo "doing the build"
rm -rf tmp
./scripts/localbuild.rb | grep -v 'aws ecr' | bash
rm -f plugins.tar *.jar

if [[ -n $1 ]]
then
    echo "running without bungee"
    TAG=no-bungee
    docker build -t $IMAGE:$TAG -f scripts/Dockerfile.nobungee .
fi
docker run \
       -e SYNDICATE_SKIP_SERVICE_CALLS=1 \
       -e SYNDICATE_ENV=development \
       -e AWS_REGION=us-west-2 \
       -e AWS_ACCESS_KEY_ID=$DEV_AWS_ACCESS_KEY_ID \
       -e AWS_SECRET_KEY=$DEV_AWS_SECRET_KEY \
       -e SYNDICATE_MATCH_QUEUE_NAME=syndicate_development_games \
       -e ECS_CONTAINER_METADATA_URI_V4=https://kenpublic.s3.amazonaws.com/2021-08-04/sAfyVbYGjZAJlKli/container-metadata.json \
       -p 25565:25565 \
       --add-host=host.docker.internal:host-gateway \
       -it \
       595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:${TAG}
