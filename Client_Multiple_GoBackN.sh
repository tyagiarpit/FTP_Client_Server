#!/bin/bash
if [ "$3" = "" ]; then
        echo "./Client_Multiple_GoBackN.sh <FILE_NAME> <HOST_NAME> <PORT>"
        exit
fi
FILE=$1
HOSTNAME=$2
PORT=$3
JAVA_CLASS=Simple_ftp_client
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
	java -cp bin $JAVA_CLASS $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;

	# Check for MSS 
	N=64
	for i in {1..10}
	do
	MSS=`echo "100*$i"|bc`
	echo "p=$p, N=$N, MSS=$MSS" >> Results.txt
	java -cp bin $JAVA_CLASS $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;


	# Check for P 
	MSS=500
	for i in {1..10}
	do
	p=`echo "0.01*$i"|bc`
	echo "p=$p, N=$N, MSS=$MSS" >> Results.txt
	java -cp bin $JAVA_CLASS $HOSTNAME $PORT $FILE $N $MSS >> Results.txt
	done;
done;

cat Results.txt|grep Transfer|cut -d" " -f4|cut -d"m" -f1 > Times.txt
