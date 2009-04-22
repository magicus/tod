#! /bin/sh

/usr/java/j2sdk1.4.2_06/bin/java -Xmx512m \
-Djava.library.path=$NATIVE \
-ea \
-server \
-cp $CLASSPATH \
-Dnode-data-dir=$DATA_DIR \
-Dmaster-host=$MASTER_HOST \
-Devents-file=$EVENTS_FILE \
$1 $2 $3 $4 $5