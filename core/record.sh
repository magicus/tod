#! /bin/sh

 /usr/lib/jvm/java-6-openjdk/bin/java -server -Xmx192m -DXtrace-filter=[+plop.**] -Dinstrumenter-log=false -Dfile.encoding=UTF-8 -Xbootclasspath:/usr/lib/jvm/java-6-openjdk/jre/lib/resources.jar:/usr/lib/jvm/java-6-openjdk/jre/lib/rt.jar:/usr/lib/jvm/java-6-openjdk/jre/lib/jsse.jar:/usr/lib/jvm/java-6-openjdk/jre/lib/jce.jar:/usr/lib/jvm/java-6-openjdk/jre/lib/charsets.jar:/usr/lib/jvm/java-6-openjdk/jre/lib/rhino.jar -classpath /home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/core/bin:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/zz.utils/bin:/home/gpothier/apps/eclipse-3.5.2/plugins/org.junit4_4.5.0.v20090824/junit.jar:/home/gpothier/apps/eclipse-3.5.2/plugins/org.hamcrest.core_1.1.0.v20090501071000.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/agent/bin:/home/gpothier/apps/eclipse-3.5.2/plugins/org.aspectj.runtime_1.6.7.20100105084524/aspectjrt.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/core/lib/jython-2.2.1.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/core/lib/infovis.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/core/lib/trove-2.0.4.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/output/eclipse:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/jd.xslt-1.5.5.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/saxon7.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/xalan-2.6.0.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/csg-bytecode.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/kawa-1.9.1.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/cojen-2.0.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/jbet3-R1.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/jclasslib.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/jiapi.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/log4j-1.2.9.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/rhino1_7R1.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/bcel-5.2.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/janino-2.5.11.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/javassist.3.6.GA.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/serp-1.14.2.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/test/lib/aspectjweaver-1.5.3.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/asm3-svn/examples/jasmin/test/jasmin.jar:/home/gpothier/apps/eclipse-3.5.2/plugins/org.junit_3.8.2.v20090203-1005/junit.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/zz.jinterp/bin:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/zz.jinterp/lib/asm-all-3.2-svn.jar:/home/gpothier/Documents/mirrored/devel/ws-tod-trunk/TOD/core/generated tod.impl.database.Recorder
