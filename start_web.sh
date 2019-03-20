#!/usr/bin/env bash
#!/bin/sh
java -jar /home/ubuntu/scaling.jar > /ubuntu/home/scaling.log &
echo $! > /home/ubuntu/web_tier.pid
/home/CSE546WebServer/apache-tomcat-8.5.38/bin/shtartup.sh
