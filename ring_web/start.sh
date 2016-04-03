#!bin/bash
cd $HOME/ring_web
nohup lein ring server >logs/log 2>logs/errors &
