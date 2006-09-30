#! /bin/sh

NODES=$1
qsub -t 1-$((NODES+1)):1 cluster-dispatch.sh $NODES 10000000