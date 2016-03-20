#!bin/bash
nohup lein ring server >logs/log 2>logs/errors &
echo "Iniciando Servidor..."
