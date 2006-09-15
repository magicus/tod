#! /bin/bash

NODES=$1
rm master-host
qsub -t 1-$((NODES+1)):1 cluster-replay.sh $NODES