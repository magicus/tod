#! /bin/sh

# This script pushes the library to a server
LOC=$1
NAME=$2

# 1. compute a signature of the svn revision of the .cpp and .h files
SIG=`svn info *.cpp *.h |grep 'Last Changed Rev:' |awk '{S += $4} END {print S}'`
echo $SIG >$NAME-sig.txt

# 2. Send to server
scp $LOC $NAME-sig.txt pleiad@pleiad.dcc.uchile.cl:/home/v/pleiad/www/files/tod/tod-agent