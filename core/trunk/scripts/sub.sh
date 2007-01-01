#! /bin/sh

source common

SCRIPT=$1
NODES=$2
SEC_NODES=$((NODES*$SUB_FACTOR))

echo NODES: $NODES SF: $SF SN: $SEC_NODES

# echo Removing lock files...
# cluster-fork rm -f $LOCK_FILE
# echo Lock files removed.

if [ -n "$SYNTAGMASTER" ] 
then
	echo "syntagma" > master-host
	qsub -t 2-$((SEC_NODES+1)):1 ./cluster-$SCRIPT.sh $NODES
	./start-$SCRIPT.sh $NODES
else
	qsub -t 1-$((SEC_NODES+1)):1 ./cluster-$SCRIPT.sh $NODES
fi

