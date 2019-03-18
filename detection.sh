#!/bin/bash
cd /home/ubuntu/darknet/
./darknet detector demo cfg/coco.data cfg/yolov3-tiny.cfg tiny.weights video.h264 -dont_show > result
python darknet.py
