#!/bin/bash

hadoop fs -mkdir /user
hadoop fs -mkdir /user/root
for file in "$@"
do
  hadoop fs -put /spark_volume/resources/"$file" /user/root/"$file"
done
