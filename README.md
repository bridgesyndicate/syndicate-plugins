# Notes

## How to enter a container running in AWS ECS

`aws ecs execute-command --cluster SyndicateECSCluster --task 93e73a3ba40b4a6bb7ca3f27451a064f --command "/bin/bash" --interactive`

### How to set it up to work

`aws ecs list-task-definitions`
`aws ecs describe-task-definition  --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/first-run-task-definition:1 | jq '.taskDefinition.executionRoleArn'`

add AmazonECSTaskExecutionRolePolicy to the `.taskDefinition.executionRoleArn`

~$ aws ecs describe-task-definition  --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/first-run-task-definition:2  | grep role
        "taskRoleArn": "arn:aws:iam::595508394202:role/syndicate-discord-bot-task-role",

role needs policy `syndicate-SSM-for-ECS-Exec`

aws ecs update-service --service arn:aws:ecs:us-west-2:595508394202:service/default/syn-bot-service --enable-execute-command
aws ecs update-service --service arn:aws:ecs:us-west-2:595508394202:service/default/syn-bot-service --force-new-deployment




## How to change an environment variable for a container
```
aws ecs list-services --service arn:aws:ecs:us-west-2:595508394202:service/default/bridge-dev-server
aws ecs describe-task-definition --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/bridge-dev-server
aws ecs describe-task-definition --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/bridge-dev-server > task.json
aws ecs register-task-definition --generate-cli-skeleton > skel.json
aws ecs register-task-definition --family bridge-dev-server --cli-input-json file://task.json
```

```json
{
        "containerDefinitions": [
            {
                "name": "bridge-dev-server",
                "image": "595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers",
                "cpu": 0,
                "portMappings": [
                    {
                        "containerPort": 25565,
                        "hostPort": 25565,
                        "protocol": "tcp"
                    }
                ],
                "essential": true,
                "environment": [
                    {
                        "name": "SYNDICATE_MATCH_QUEUE_NAME",
                        "value": "syndicate_production_games"
                    }
                ],
                "mountPoints": [],
                "volumesFrom": [],
                "logConfiguration": {
                    "logDriver": "awslogs",
                    "options": {
                        "awslogs-group": "/ecs/bridge-dev-server",
                        "awslogs-region": "us-west-2",
                        "awslogs-stream-prefix": "ecs"
                    }
                }
            }
        ],
        "family": "bridge-dev-server",
        "taskRoleArn": "arn:aws:iam::595508394202:role/syndicate-bridge-dev-server-task-role",
        "executionRoleArn": "arn:aws:iam::595508394202:role/ecsTaskExecutionRole",
        "networkMode": "awsvpc",
        "volumes": [],
        "placementConstraints": [],
        "requiresCompatibilities": [
            "FARGATE"
        ],
        "cpu": "512",
        "memory": "1024"
}
```

```
aws ecs update-service --service arn:aws:ecs:us-west-2:595508394202:service/default/bridge-dev-server --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/bridge-dev-server:5
aws ecs deregister-task-definition --task-definition arn:aws:ecs:us-west-2:595508394202:task-definition/bridge-dev-server:4
```
