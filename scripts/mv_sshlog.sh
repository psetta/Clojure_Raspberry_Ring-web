#!/bin/bash

cp /var/log/auth.log /home/psetta/logs/cpauth.log

cd /home/psetta/logs

if ! [ -f sshlog_aux.log ]; then
  touch sshlog_aux.log
fi
if ! [ -f sshlog0.log ]; then
  touch sshlog0.log
fi

cat cpauth.log | grep 'Invalid user' >> sshlog_aux.log
cat sshlog_aux.log | sort | uniq > log_temp.log
mv log_temp.log sshlog_aux.log

sort -o sshlog_aux.log sshlog_aux.log
sort -o sshlog0.log sshlog0.log

comm -23 sshlog_aux.log sshlog0.log > sshlog_new.log

cat sshlog_aux.log | sort | uniq > sshlog0.log

cp sshlog0.log sshlog_aux.log

