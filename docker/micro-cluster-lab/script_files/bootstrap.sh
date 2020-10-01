#!/bin/bash

hdfs namenode -format
service ssh start
if [ "$HOSTNAME" = node-master ]; then
    start-dfs.sh
    start-yarn.sh
    #start-master.sh
    cd /root/lab
    /opt/hadoop/bin/hadoop fs -put /spark_volume/spark_example.txt /spark_example.txt
    /opt/spark/bin/spark-submit --class Example --master yarn /spark_volume/java-spark-1.0-SNAPSHOT.jar /spark_example.txt
fi
#bash
while :; do :; done & kill -STOP $! && wait $!