#!/bin/sh
java -jar /home/ubuntu/app_tier.jar > /home/ubuntu/app_tier.log &
echo $! > /home/ubuntu/app_tier.pid