#!/bin/bash

sudo lsof -t -i:80 | sudo xargs kill -9

export MICRONAUT_SERVER_PORT=80

nohup sudo java -jar ~/esop-trading-application/esop-trading-0.1-all.jar > /dev/null &