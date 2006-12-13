#! /bin/sh

AGENT=./libbci-agent.so
CLASSPATH=./bin:../zz.utils/bin

HOST=localhost
#HOST=syntagma.dim.uchile.cl


VMARGS=''
VMARGS="$VMARGS -agentpath:$AGENT"
VMARGS="$VMARGS -noverify"
VMARGS="$VMARGS -Dcollector-host=$HOST -Dcollector-port=8058 -Dnative-port=8059 -Dtod-host=tod-1"
VMARGS="$VMARGS -Dcollector-type=socket"
VMARGS="$VMARGS -Xbootclasspath/p:./bin" 
VMARGS="$VMARGS -ea" 
VMARGS="$VMARGS -server" 
VMARGS="$VMARGS -Xmx384m" 
VMARGS="$VMARGS -XX:MaxPermSize=128m"
#VMARGS="$VMARGS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

#java $VMARGS -cp ./bin dummy.Dummy
~/apps/eclipse-3.2.1/eclipse -data ~/eclipse/ws-tod -consolelog -vmargs $VMARGS
#java $VMARGS -cp ./bin calls.Main


#Machines:
# ireul
# arael
# bardiel
# naud2
# leliel
# pilmaiquen