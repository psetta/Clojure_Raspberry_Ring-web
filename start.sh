#!bin/bash
cd $HOME/www/clojure_ring_pi
nohup lein ring server >logs/log 2>logs/errors &
