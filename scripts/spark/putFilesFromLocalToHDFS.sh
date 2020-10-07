#!/bin/bash
SPARK_RESOURCES_DIR="/spark_volume/resources"
echo "Adding /user dir to hadoop"
hadoop fs -mkdir /user
echo "Adding /user/root dir to hadoop"
hadoop fs -mkdir /user/root
for file in "$@"
do
  echo "Putting $SPARK_RESOURCES_DIR/$file from local into hdfs:///user/root/$file"
  hadoop fs -put $SPARK_RESOURCES_DIR/"$file" /user/root/
done
