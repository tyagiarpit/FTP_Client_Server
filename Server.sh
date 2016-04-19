#!/bin/bash
PORT=7783
FILE=_destination.pdf

for r in {1..5}
do
	p=0.05
	#Check for N
	for i in {0..10}
	do
	java -cp bin Simple_ftp_server $PORT N$r$i$FILE $p
	done;

	#Check for MSS 
	for i in {1..10}
	do
	java -cp bin Simple_ftp_server $PORT MSS$r$i$FILE $p
	done;

	#Check for N
	for i in {1..10}
	do
	p=`echo "0.01*$i"|bc`
	java -cp bin Simple_ftp_server $PORT P$r$i$FILE $p
	done;
done;

