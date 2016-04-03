#!#!/usr/bin/env python

from re import findall
import socket

dir_log0 = "/home/psetta/logs"
dir_log1 = "/home/psetta/ring_web/static"

log0 = open(dir_log0+"/sshlog_new.log","r").read()

log_final = findall(".+Invalid user.+",log0)

sshlog = open(dir_log1+"/sshlog.log","a")

hostname_dict = {}

for linha in log_final:
	fila_log = findall("(.+)pi[^pi].+Invalid\suser(.+)from(.+)",linha)[0]
	fila_log = [x.strip() for x in fila_log]
	ip = fila_log[2]
	if ip in hostname_dict:
		hostname = hostname_dict[ip]
	else:
		try:
			hostname = socket.gethostbyaddr(ip)[0]
		except:
			hostname = "none"
		hostname_dict[ip] = hostname
	fila_log.append(hostname)
	sshlog.write(", ".join(fila_log)+"\n")
	
sshlog.close()
