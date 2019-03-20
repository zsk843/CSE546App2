#!/usr/bin/env bash
PID=$(cat /home/ubuntu/web_tier.pid)
kill -9 $PID