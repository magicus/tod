#! /bin/sh

AGENT=../TOD-agng/libtod-agent.so
#AGENT=./libbci-agent.so
#CLASSPATH=./bin:../zz.utils/bin

HOST=localhost
#HOST=syntagma.dim.uchile.cl
#HOST=padme.dcc.uchile.cl


VMARGS=''
VMARGS="$VMARGS -agentpath:$AGENT"
VMARGS="$VMARGS -noverify"
VMARGS="$VMARGS -Dcollector-host=$HOST -Dcollector-port=8158 -Dnative-port=8159 -Dtod-host=tod-1"
VMARGS="$VMARGS -Dcollector-type=socket"
VMARGS="$VMARGS -Xbootclasspath/p:./bin" 
#VMARGS="$VMARGS -ea" 
VMARGS="$VMARGS -server" 
VMARGS="$VMARGS -Xmx384m" 
VMARGS="$VMARGS -XX:MaxPermSize=128m"
VMARGS="$VMARGS -Dagent-verbose=3"
#VMARGS="$VMARGS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

java $VMARGS -cp ./bin dummy.Dummy
# echo "set args $VMARGS -cp ./bin dummy.Dummy" > gdb.cmd
# gdb -x gdb.cmd java

# echo "set args -jar /home/gpothier/apps/jabref/JabRef-2.2.jar" > gdb.cmd
# gdb  -x gdb.cmd java
#java $VMARGS -jar /home/gpothier/apps/jabref/JabRef-2.2.jar

#java $VMARGS -cp "../../runtime-EclipseApplication(1)/TODTest/bin/":lib/zz.utils.jar imageviewer2.ImageViewer $1
#java $VMARGS -cp ./bin dummy.Dummy2

#~/apps/eclipse-3.3.1.1/eclipse -vm /home/gpothier/apps/java/jdk1.5.0_08/bin/java -data ~/eclipse/ws-tod -consolelog -vmargs $VMARGS

# /home/gpothier/apps/java/jdk1.6.0_01/bin/java $VMARGS -jar /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar -os linux -ws gtk -arch x86 -showsplash -launcher /home/gpothier/apps/eclipse-3.3.1.1/eclipse -name Eclipse --launcher.library /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_1.0.2.R331_v20071019/eclipse_1021.so -startup /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar -exitdata 2d88001 -data ~/eclipse/ws-tod -consolelog -vm /home/gpothier/apps/java/jdk1.5.0_08/bin/java -vmargs -Xmx256m -XX:MaxPermSize=128m -jar /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar

# echo "handle SIGSEGV nostop noprint
# set args $VMARGS -jar /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar -os linux -ws gtk -arch x86 -showsplash -launcher /home/gpothier/apps/eclipse-3.3.1.1/eclipse -name Eclipse --launcher.library /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_1.0.2.R331_v20071019/eclipse_1021.so -startup /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar -exitdata 2d88001 -data ~/eclipse/ws-tod -consolelog -vm /home/gpothier/apps/java/jdk1.5.0_08/bin/java -vmargs -Xmx256m -XX:MaxPermSize=128m -jar /home/gpothier/apps/eclipse-3.3.1.1/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar" > gdb.cmd
# gdb -x gdb.cmd /home/gpothier/apps/java/jdk1.5.0_08/bin/java 
#java $VMARGS -cp ./bin calls.Main


#Machines:
# ireul
# arael
# bardiel
# naud2
# leliel
# pilmaiquen
