#! /bin/sh

AGENT=./libbci-agent.so
CLASSPATH=./bin

HOST=localhost
#HOST=syntagma.dim.uchile.cl
java -agentpath:$AGENT -noverify -cp $CLASSPATH -Dcollector-host=$HOST -Dcollector-port=8058 -Dnative-port=8059 -Dtod-host=tod-1 dummy.Dummy
