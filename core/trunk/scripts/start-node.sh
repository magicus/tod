#! /bin/sh

source common

MASTER_HOST=$1
NODES=$2
SEC_NODES=$((SUB_FACTOR*NODES))

./launch.sh node $MASTER_HOST $SEC_NODES
