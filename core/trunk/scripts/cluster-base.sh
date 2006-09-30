echo JobID: $JOB_ID - Task ID: $SGE_TASK_ID

if [ $SGE_TASK_ID -eq 1 ] 
then
	echo MASTER
	MASTER_HOST=`hostname`
	echo $MASTER_HOST > master-host.$JOB_ID
	echo Starting master, host: $MASTER_HOST
	./$1 $2 $3
	rm master-host.$JOB_ID
else
	echo NODE
	sleep 5s
	until [ -e master-host.$JOB_ID ] 
	do 
		echo Waiting...
		sleep 1s 
	done
	MASTER_HOST=`cat master-host.$JOB_ID`
	echo Starting node, connecting to $MASTER_HOST
	sleep 5s
	./start-node.sh $MASTER_HOST
fi


