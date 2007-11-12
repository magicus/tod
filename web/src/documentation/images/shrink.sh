#! /bin/bash

for n in $*
do
  base=`strip-suffix $n`
  convert $n -scale 400 -quality 50 $base-s.jpg
  convert $n -scale 400 $base-s.png
done