We support transport data to tigergraph from mysql, sqlserver, oracle and GCS.

Connector based on Datax.
# DataX Introduction
##### Reference：[DataX-Introduction](https://github.com/alibaba/DataX/blob/master/introduction.md)

# Package
mvn clean package -DskipTests assembly:assembly 

# Oracle Reader
If you need oraclereader, you should add oraclereader module in parent project pom file.
Then install the right version of ojdbc6 in your local maven repository which defined in oraclereader pom file. 

# Config job
Reference to resources/plugin_job_template.json in each reader or writer.
Examples : core/src/job/*

# Execute job 
###python 2 : 
cd ~/datax/bin
python datax.py ../job/job.json
###python 3 :
cd ~datax/bin/python3
python datax.py ../../job/job.json

