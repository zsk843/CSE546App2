#!/usr/bin/env bash
#!/bin/sh
java -jar /home/ubuntu/scaling.jar > /home/ubuntu/scaling.log &
echo $! > /home/ubuntu/web_tier.pid
/home/ubuntu/CSE546WebServer/apache-tomcat-8.5.38/bin/startup.sh
