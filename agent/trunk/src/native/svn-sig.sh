#! /bin/sh
# Compute a signature of the svn revision of the .cpp and .h files
cat *.cpp *.h | md5sum  | awk '{print $1}'
