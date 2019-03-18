#!/bin/sh
PID=$(cat /home/ubuntu/app_tier.pid)
kill -9 $PID