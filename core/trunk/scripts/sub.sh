#! /bin/sh

SCRIPT=$1
NODES=$2
NODES=$((15*NODES/10))

# echo Removing lock files...
# cluster-fork rm -f $LOCK_FILE
# echo Lock files removed.

if [ -n "$SYNTAGMASTER" ] 
then
	echo "syntagma" > master-host
	qsub -t 2-$((NODES+1)):1 cluster-$SCRIPT.sh $NODES
	./start-$SCRIPT.sh $NODES
else
	qsub -t 1-$((NODES+1)):1 cluster-$SCRIPT.sh $NODES
fi

