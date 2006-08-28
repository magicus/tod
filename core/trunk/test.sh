#! /bin/sh

AGENT=./libbci-agent.so
CLASSPATH=./bin

java -agentpath:$AGENT -noverify -cp $CLASSPATH -Dcollector-host=localhost -Dcollector-port=8058 -Dnative-port=8059 -Dtod-host=tod-1 dummy.Dummy
