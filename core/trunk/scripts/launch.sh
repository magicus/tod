#! /bin/sh

case "$1" in
	"session"	) MAIN="tod.impl.dbgrid.GridSession";;
	"node"		) MAIN="tod.impl.dbgrid.dbnode.DatabaseNode";;
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
	

$JAVA_HOME/bin/java -Xmx512m\
 -Djava.library.path=$NATIVE\
 -ea\
 -server\
 -cp $CLASSPATH\
 -Dnode-data-dir=$DATA_DIR\
 -Dmaster-host=$MASTER_HOST\
 -Devents-file=$EVENTS_FILE\
 -Dlocations-file=$LOCATIONS_FILE\
 $MAIN $2 $3 $4 $5