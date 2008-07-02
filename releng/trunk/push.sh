#! /bin/sh

# This script pushes the release to a server

scp release/* pleiad@pleiad.dcc.uchile.cl:/home/v/pleiad/www/files/tod/releases
VERSION=`ls release/changes_*.txt |awk '{print substr($0, 17, length($0)-20)}'`
echo ./releng.py -c $VERSION
