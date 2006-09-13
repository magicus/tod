#! /bin/sh

source common

java -cp $CLASSPATH\
 -Djava.library.path=".."\
 -Xmx512m\
 -ea\
 -Devents-file="../events-raw.bin"\
 -server\
 tod.impl.dbgrid.bench.GridQuery
