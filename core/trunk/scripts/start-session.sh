#! /bin/sh

java -Xmx512m\
 -Djava.library.path=.\
 -cp .:asm-2.1.jar:asm-commons-2.1.jar:backport-util-concurrent.jar:javassist.jar:retrotranslator-runtime-1.0.8.jar,:retrotranslator-transformer-1.0.8.jar:tod-agent.jar:tod-debugger.jar:zz.utils.jar\
 session