#!/bin/bash
MAIN_CLASS=$1
JAVA_ARGS=("${@:2}")
echo "Submitting $MAIN_CLASS with args:"
echo "${JAVA_ARGS[*]}"
/opt/spark/bin/spark-submit --class $MAIN_CLASS --master yarn \
 /spark_volume/jars/target/spark-jobs-1.0-SNAPSHOT-jar-with-dependencies.jar ${JAVA_ARGS[*]}
