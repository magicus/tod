#! /bin/sh

DATA_DIR=/state/partition1/gpothier/tod

/usr/java/j2sdk1.4.2_06/bin/java -Xmx512m \
-Djava.library.path=. \
-ea \
-server \
-cp .:asm-2.1.jar:asm-commons-2.1.jar:backport-util-concurrent.jar:javassist.jar:retrotranslator-runtime-1.0.8.jar,:retrotranslator-transformer-1.0.8.jar:tod-agent.jar:tod-debugger.jar:tod-test.jar:zz.utils.jar \
-Dnode-data-dir=$DATA_DIR \
Retro nodestore