#!#!/usr/bin/env python
# -*- coding: utf-8 -*-

from re import findall
import socket
import geoip2.database as geo

dir_log0 = "/home/psetta/logs"
dir_log1 = "/home/psetta/ring_web/static"

ip_db = geo.Reader("/home/psetta/countryIP_db/GeoLite2-City.mmdb")

log0 = open(dir_log0+"/sshlog_new.log","r").read()

log_final = findall(".+Invalid user.+",log0)

sshlog = open(dir_log1+"/sshlog.log","a")

hostname_dict = {}

for linha in log_final:
	fila_log = findall("(.+)pi[^pi].+Invalid\suser(.+)from(.+)",linha)[0]
	fila_log = [x.strip() for x in fila_log]
	ip = fila_log[2]
	ip_info = ip_db.city(ip)
	if ip in hostname_dict:
		hostname = hostname_dict[ip]
	else:
		try:
			hostname = socket.gethostbyaddr(ip)[0]
		except:
			hostname = ""
		hostname_dict[ip] = hostname
	try:
		country = ip_info.country.names["es"]
	except:
		country = ""
	try:
		city = ip_info.city.names["en"]
	except:
		city = ""
	fila_log.append(city)
	fila_log.append(country)
	fila_log.append(hostname)
	sshlog.write(", ".join(fila_log).encode("utf-8")+"\n")
	
sshlog.close()
