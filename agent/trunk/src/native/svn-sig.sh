#! /bin/sh
# Compute a signature of the svn revision of the .cpp and .h files
FILES="bci-agent.cpp jniutils.cpp jniutils.h md5.cpp md5.h md5_loc.h utils.cpp utils.h"

cat $FILES | md5sum  | awk '{print $1}'
