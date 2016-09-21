#!/bin/bash
export JAVA_HOME=/usr/java/jdk1.7.0_79/
export HADOOP_HOME=/usr/lib/hadoop/
export HIVE_HOME=/home/centos/apache-hive-1.2.1-bin/
export PATH=$HADOOP_HOME/bin:$JAVA_HOME/bin:$PATH:$HIVE_HOME/bin
cd /home/centos/vopEngine/updatedBin


today=`date +"%d-%m-%Y"`

echo "NLP Entity Generation Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar NLPEntitiesGeneration-job.jar -inputTable parseddata -outputTable parseddata -newbatch >> ../log/NLPJar-$today.log
echo "NLP Entity Generation Ended at "`date` >> ../log/NLPJar-$today.log

echo "Drug feature Extraction Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar DrugSideEffectFeatureExtraction-job.jar  -inputTable parseddata -outputTable drugSegments -all >> ../log/NLPJar-$today.log
echo "Drug feature Extraction Ended at "`date` >> ../log/NLPJar-$today.log

echo "Alt Drug feature Extraction Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar AltDrugSideEffectFeatureExtraction-job.jar -inputTable parseddata -outputTable altDrugSegments -all >> ../log/NLPJar-$today.log
echo "Alt Drug feature Extraction Ended at "`date` >> ../log/NLPJar-$today.log

echo "Alt Therapy feature Extraction Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar AltTherapySideEffectFeatureExtraction-job.jar -inputTable parseddata -outputTable altTherapySegments -all >> ../log/NLPJar-$today.log
echo "Alt Therapy feature Extraction Ended at "`date` >> ../log/NLPJar-$today.log

echo "Regimen feature Extraction Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar RegimenSideEffectFeatureExtraction-job.jar -inputTable parseddata -outputTable regimenSegments -all >> ../log/NLPJar-$today.log
echo "Regimen feature Extraction Ended at "`date` >> ../log/NLPJar-$today.log

echo "Drug SE classification Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar DrugSideEffectClassification-job.jar -messageTable parseddata -segmentTable drugSegments -all >> ../log/NLPJar-$today.log
echo "Drug SE classification Ended at "`date` >> ../log/NLPJar-$today.log

echo "Alt Drug SE classification Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar AltDrugSideEffectClassification-job.jar -messageTable parseddata -segmentTable altDrugSegments -all >> ../log/NLPJar-$today.log
echo "Alt Drug SE classification Ended at "`date` >> ../log/NLPJar-$today.log

echo "Alt Therapy SE classification Started at "`date` >> ../log/NLPJar-$today.log
hadoop jar AltTherapySideEffectClassification-job.jar -messageTable parseddata -segmentTable altTherapySegments -all  >> ../log/NLPJar-$today.log
echo "Alt Therapy SE classification Ended at "`date` >> ../log/NLPJar-$today.log

echo "Hive table creation Started at "`date` >> ../log/HiveTable-$today.log
$HIVE_HOME/bin/hive --auxpath $HIVE_HOME/lib/,/var/lib/hbase-0.98.16.1-hadoop2/lib/ --hiveconf hbase.master=localhost:60010,mapreduce.map.memory.mb=4096,mapreduce.reduce.memory.mb=5120,hive.auto.convert.join=true -f hive_scripts/create_tables >> ../log/HiveTable-$today.log
echo "Hive table creation Ended at "`date` >> ../log/HiveTable-$today.log



echo "Result merge query execution Started at "`date` >> ../log/HiveTable-$today.log

$HIVE_HOME/bin/hive --auxpath $HIVE_HOME/lib/,/var/lib/hbase-0.98.16.1-hadoop2/lib/ --hiveconf hbase.master=localhost:60010,mapreduce.map.memory.mb=4096,mapreduce.reduce.memory.mb=5120,hive.auto.convert.join=true -f hive_scripts/hive_queries >> ../log/HiveTable-$today.log

$HIVE_HOME/bin/hive --auxpath $HIVE_HOME/lib/,/var/lib/hbase-0.98.16.1-hadoop2/lib/ --hiveconf hbase.master=localhost:60010,mapreduce.map.memory.mb=4096,mapreduce.reduce.memory.mb=5120,hive.auto.convert.join=true -e 'select * from temp' > hive_scripts/keys

flag=2
echo "INSERT INTO parseddata values " >> hive_scripts/insertQueries

for i in `cat hive_scripts/keys`; do echo "(NULL,NULL,NULL,NULL,NULL,NULL,'$flag',NULL,NULL,NULL,NULL,NULL,NULL,'$i')," >> hive_scripts/insertQueries; done;

sed -i '$ s/.$//' hive_scripts/insertQueries

echo ";" >> hive_scripts/insertQueries


mkdir processFlagScripts
cp hive_scripts/insertQueries processFlagScripts
cd processFlagScripts
split -l 10000 insertQueries
sed -i '1s/^/INSERT INTO parseddata values/' *
rm insertQueries
sed -i 's/INSERT INTO parseddata valuesINSERT INTO parseddata values/INSERT INTO parseddata values/g' *
sed -i '$ s/.$/;/' *
cd ..

for i in `ls processFlagScripts`;
do
$HIVE_HOME/bin/hive --auxpath $HIVE_HOME/lib/,/var/lib/hbase-0.98.16.1-hadoop2/lib/ --hiveconf hbase.master=localhost:60010,mapreduce.map.memory.mb=4096,mapreduce.reduce.memory.mb=5120,hive.auto.convert.join=true -f processFlagScripts/$i
done;

rm hive_scripts/keys hive_scripts/insertQueries processFlagScripts -R



echo "Result merge query execution Ended at "`date` >> ../log/HiveTable-$today.log