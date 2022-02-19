#!/bin/bash
set -e
VERSION=17 # these are versioned in s3 in case of a fuck up
REPOSITORY_HOST=595508394202.dkr.ecr.us-west-2.amazonaws.com
REPOSITORY_URI=$REPOSITORY_HOST/syn-minecraft-dist
S3_BUCKET=syndicate-versioned-artifacts
BASE_DOCKER=minecraft-home

if [ -d "${BASE_DOCKER}" ]; then
    echo building version $VERSION with the contents of $BASE_DOCKER
    echo and copying to s3://${S3_BUCKET}/
    echo ctrl-c if that\'s not what you want, otherwise, return to cont.
    read cont
    tar -cJf ${BASE_DOCKER}.${VERSION}.tar.xz $BASE_DOCKER
    aws s3 cp ${BASE_DOCKER}.${VERSION}.tar.xz s3://${S3_BUCKET}/
    mv ${BASE_DOCKER}.${VERSION}.tar.xz ${BASE_DOCKER}.tar.xz
    aws ecr get-login-password  | docker login --username AWS --password-stdin $REPOSITORY_HOST
    docker build -t $REPOSITORY_URI:latest .
    docker push $REPOSITORY_URI:latest
else
    echo downloading version $VERSION of $BASE_DOCER from s3 to unpack at $BASE_DOCKER
    echo ctrl-c if that\'s not what you want, otherwise, return to cont.
    read cont
    aws s3 cp s3://${S3_BUCKET}/${BASE_DOCKER}.${VERSION}.tar.xz $BASE_DOCKER.tar.xz
    tar -xf $BASE_DOCKER.tar.xz
fi
