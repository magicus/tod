#! /bin/sh

NODES=$1
qsub -t 1-$((NODES+1)):1 ./cluster-query.sh $NODES