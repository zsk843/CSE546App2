#!/bin/sh
PID=$(cat /ubuntu/home/app_tier.pid)
kill -9 $PID