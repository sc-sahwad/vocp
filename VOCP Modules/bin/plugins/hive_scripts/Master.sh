#!/bin/bash
export JAVA_HOME=/usr/java/jdk1.7.0_79/
export HADOOP_HOME=/usr/lib/hadoop/
export HIVE_HOME=/home/centos/apache-hive-1.2.1-bin/
export PATH=$HADOOP_HOME/bin:$JAVA_HOME/bin:$PATH:$HIVE_HOME/bin
cd /home/centos/vopEngine/updatedBin

today=`date +"%d-%m-%Y"`

if ps -ef | grep -v grep | grep executeFlow ; then
	echo "Script already running"  >> ../log/Master-$today.log
        exit 0
else
        echo "Starting script at "`date`  >> ../log/Master-$today.log
        ./executeFlow.sh
        echo "NLP Pipeline Execution completed at "`date` | mail -s "NLP Pipeline Execution" sagar.chaturvedi@scryanalytics.com
        echo "Script completed at "`date`  >> ../log/Master-$today.log
        exit 0
fi




