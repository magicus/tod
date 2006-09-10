#! /bin/sh

deploy()
{
	HOST=$1
	DIR=$2
	
	rsync -avz -e ssh\
	 --rsync-path '/usr/bin/rsync --server --daemon --config=$HOME/rsync.conf .'\
	  $DIR\
	  gpothier@$HOST::tod
}

deploy syntagma.dim.uchile.cl $1 || exit 10
deploy dichato $1 || exit 11
exit 0