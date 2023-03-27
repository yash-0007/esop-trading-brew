#!/bin/bash

sudo lsof -t -i:80 | sudo xargs kill -9

sudo yum install java-17-amazon-corretto-headless -y

nohup sudo MICRONAUT_SERVER_PORT=80 java -jar ~/esop-trading-application/esop-trading-0.1-all.jar /dev/null 2>&1 &