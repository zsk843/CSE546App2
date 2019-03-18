#!/bin/sh
java -jar /ubuntu/home/app_tier.jar > /ubuntu/home/app_tier.log &
echo $! > /ubuntu/home/app_tier.pid