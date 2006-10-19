#! /bin/sh

SCRIPT=$1
NODES=$2

# echo Removing lock files...
# cluster-fork rm -f $LOCK_FILE
# echo Lock files removed.

if [ -n "$SYNTAGMASTER" ] 
then
	echo "syntagma" > master-host
	qsub -t 2-$((NODES+5)):1 cluster-$SCRIPT.sh $NODES
	./start-$SCRIPT.sh $NODES
else
	qsub -t 1-$((NODES+5)):1 cluster-$SCRIPT.sh $NODES
fi

