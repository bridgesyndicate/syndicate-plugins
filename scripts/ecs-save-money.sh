#!/bin/bash
set -evx
if [[ -z $1 ]]
then
    echo 'set the number of desired instances'
    echo 'e.g:'
    echo $0 1
else
    CLUSTER=SyndicateECSCluster
    readarray -t arns < <(aws ecs list-services --cluster $CLUSTER | \
			      jq -r '.serviceArns[]')
    for arn in "${arns[@]}"
    do
	echo setting $arn
	aws ecs update-service \
	    --service $arn \
	    --cluster $CLUSTER \
	    --desired-count $1 | \
	    jq '.service.desiredCount'
    done
fi
exit
