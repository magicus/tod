#! /bin/sh

case "$1" in
	"session"	) MAIN="tod.impl.dbgrid.GridSession";;
	"master"	) MAIN="tod.impl.dbgrid.GridMaster";;
	"node"		) MAIN="tod.impl.dbgrid.StartNode";;
	"store"		) MAIN="tod.utils.StoreTODServer";;
	"replay"	) MAIN="tod.impl.dbgrid.bench.GridReplay";;
	"query"		) MAIN="tod.impl.dbgrid.bench.GridQuery";;
	"nodestore"	) MAIN="tod.impl.dbgrid.bench.BenchDatabaseNode";;
	"dispatch"	) MAIN="tod.impl.dbgrid.bench.GridDispatch";;
	"netbench"	) MAIN="tod.impl.dbgrid.bench.NetBench";;
	*			)
		echo Not recognized: $1
		exit 1;;
esac
	
VMARGS=''
VMARGS="$VMARGS -Xmx$JVM_HEAP_SIZE_SIZE"
VMARGS="$VMARGS -Djava.library.path=$NATIVE"
VMARGS="$VMARGS -ea"
VMARGS="$VMARGS -server"
VMARGS="$VMARGS -cp $CLASSPATH"
VMARGS="$VMARGS -Dnode-data-dir=$DATA_DIR"
VMARGS="$VMARGS -Dmaster-host=$MASTER_HOST"
VMARGS="$VMARGS -Devents-file=$EVENTS_FILE"
VMARGS="$VMARGS -Dlocations-file=$LOCATIONS_FILE"
VMARGS="$VMARGS -Dpage-buffer-size=$PAGE_BUFFER_SIZE"
VMARGS="$VMARGS -Dtask-id=$TASK_ID"
VMARGS="$VMARGS -Dgrid-impl=$GRID_IMPL"
VMARGS="$VMARGS -Dcheck-same-host=$CHECK_SAME_HOST"
VMARGS="$VMARGS $EXTRA_JVM_ARGS"

if [ -n "$JDWP_PORT" ]
then
	VMARGS="$VMARGS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=$JDWP_PORT"
fi

echo Host: `hostname`
#$JAVA_HOME/bin/java -version 
$JAVA_HOME/bin/java $VMARGS $MAIN $2 $3 $4 $5