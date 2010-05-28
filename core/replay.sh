#! /bin/sh

[ -n "$BASE" ] || export BASE=`pwd`/../..
[ -n "$ECLIPSE_HOME" ] || export ECLIPSE_HOME=/home/gpothier/apps/eclipse-3.5.2
[ -n "$MEM_HEAP" ] || export MEM_HEAP=768m
[ -n "$MEM_PERMSIZE" ] || export MEM_PERMSIZE=384m
[ -n "$REPLAYER" ] || export REPLAYER=tod.impl.server.DBSideIOThread
[ -n "$JAVA_HOME" ] || export JAVA_HOME=/home/gpothier/apps/java/jdk1.6.0_16/
$JAVA_HOME/bin/java -DX=agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:52772 -server -Xmx$MEM_HEAP -ea -XX:MaxPermSize=$MEM_PERMSIZE -Dfile.encoding=UTF-8 -classpath $BASE/TOD/core/bin:$BASE/zz.utils/bin:$ECLIPSE_HOME/plugins/org.junit4_4.5.0.v20090824/junit.jar:$ECLIPSE_HOME/plugins/org.hamcrest.core_1.1.0.v20090501071000.jar:$BASE/TOD/agent/bin:$ECLIPSE_HOME/plugins/org.aspectj.runtime_1.6.7.20100105084524/aspectjrt.jar:$BASE/TOD/core/lib/jython-2.2.1.jar:$BASE/TOD/core/lib/infovis.jar:$BASE/TOD/core/lib/trove-2.1.0.jar:$BASE/asm3-svn/output/eclipse:$BASE/asm3-svn/test/lib/jd.xslt-1.5.5.jar:$BASE/asm3-svn/test/lib/saxon7.jar:$BASE/asm3-svn/test/lib/xalan-2.6.0.jar:$BASE/asm3-svn/test/lib/csg-bytecode.jar:$BASE/asm3-svn/test/lib/kawa-1.9.1.jar:$BASE/asm3-svn/test/lib/cojen-2.0.jar:$BASE/asm3-svn/test/lib/jbet3-R1.jar:$BASE/asm3-svn/test/lib/jclasslib.jar:$BASE/asm3-svn/test/lib/jiapi.jar:$BASE/asm3-svn/test/lib/log4j-1.2.9.jar:$BASE/asm3-svn/test/lib/rhino1_7R1.jar:$BASE/asm3-svn/test/lib/bcel-5.2.jar:$BASE/asm3-svn/test/lib/janino-2.5.11.jar:$BASE/asm3-svn/test/lib/javassist.3.6.GA.jar:$BASE/asm3-svn/test/lib/serp-1.14.2.jar:$BASE/asm3-svn/test/lib/aspectjweaver-1.5.3.jar:$BASE/asm3-svn/examples/jasmin/test/jasmin.jar:$ECLIPSE_HOME/plugins/org.junit_3.8.2.v20090203-1005/junit.jar:$BASE/zz.jinterp/bin:$BASE/zz.jinterp/lib/asm-all-3.2-svn.jar:$BASE/TOD/core/generated $REPLAYER

