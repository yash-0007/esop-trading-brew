#!/bin/bash

#sudo kill -9 $(lsof -t -i:80)

export MICRONAUT_SERVER_PORT=80

sudo yum install java-17-amazon-corretto-headless -y > /dev/null

nohup sudo java -jar ~/esop-trading-application/esop-trading-0.1-all.jar > /dev/null &