#!/bin/bash

sudo lsof -t -i:80 | sudo xargs kill -9

nohup sudo MICRONAUT_SERVER_PORT=80 java -jar /home/ec2-user/esop-trading-0.1-all.jar > /dev/null &
