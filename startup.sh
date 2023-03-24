#!/bin/bash

sudo lsof -t -i:8080 | sudo xargs kill -9

export MICRONAUT_SERVER_PORT=8080

nohup sudo java -jar ~/esop-trading-application/esop-trading-0.1-all.jar > /dev/null &