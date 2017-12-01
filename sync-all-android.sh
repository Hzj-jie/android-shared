#!/bin/bash

for ((;;))
do
  for i in $(ls -1 | grep -F "android-")
  do
    cd $i
    git pull
    cd ..
  done;
  sleep 10m
done
