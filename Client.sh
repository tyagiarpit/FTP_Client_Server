#!/bin/bash

FILE=source.pdf
HOSTNAME=localhost
PORT=7783

# Check for N
rm Results.txt

for r in {1..5}
do
	p=0.05
	MSS=500
	echo "Round :$r" >> Results.txt
	for i in {0..10}
	do
	N=`echo "2^$i"|bc`
	echo "p=$p, N=$N, MSS=$MSS" >> Results.txt
	java -cp bin Simple_ftp_client_sr $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;

	# Check for MSS 
	N=64
	for i in {1..10}
	do
	MSS=`echo "100*$i"|bc`
	echo "p=$p, N=$N, MSS=$MSS" >> Results.txt
	java -cp bin Simple_ftp_client_sr $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;


	# Check for P 
	MSS=500
	for i in {1..10}
	do
	p=`echo "0.01*$i"|bc`
	echo "p=$p, N=$N, MSS=$MSS" >> Results.txt
	java -cp bin Simple_ftp_client_sr $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;
done;

cat Results.txt|grep Transfer|cut -d" " -f4|cut -d"m" -f1 > Times.txt
