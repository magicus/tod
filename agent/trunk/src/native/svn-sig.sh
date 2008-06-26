#! /bin/sh
# Compute a signature of the svn revision of the .cpp and .h files
svn info *.cpp *.h |grep 'Last Changed Rev:' |awk '{S += $4} END {print S}'