#!bin/bash
pid=$(ps -x | grep ring_web | awk '{ print $1 }')
kill $pid 2>/dev/null
