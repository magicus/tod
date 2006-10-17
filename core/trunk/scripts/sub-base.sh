#! /bin/sh

SCRIPT=$1
NODES=$2

if [ $SYNTAGMASTER -eq 1 ] 
then
	echo "syntagma" > master-host
	qsub -t 2-$((NODES+1)):1 cluster-$SCRIPT.sh $NODES
	./start-$SCRIPT.sh $NODES
else
	qsub -t 1-$((NODES+1)):1 cluster-$SCRIPT.sh $NODES
fi

