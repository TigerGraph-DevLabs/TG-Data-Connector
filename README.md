We support transport data to tigergraph from mysql, sqlserver, oracle and GCS.

Connector based on Datax.
# DataX Introduction
##### Reference：[DataX-Introduction](https://github.com/alibaba/DataX/blob/master/introduction.md)

# Development environment
Java 8+
Maven 3.5.4+
Python 2|3, both supported

# Package
Download source code then running following command.
```shell script
mvn clean package -DskipTests assembly:assembly 
```
## Package structure
├── datax
│   ├── bin
│   ├── conf
│   ├── job
│   ├── lib
│   ├── log
│   ├── log_perf
│   ├── plugin
│   └── script

# Execute job 
### python 2 : 
cd ~/datax/bin
python datax.py ../job/job.json
### python 3 :
cd ~datax/bin/python3
python datax.py ../../job/job.json

# Config job
Reference to resources/plugin_job_template.json in each reader or writer.
[Job Examples](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/tree/main/core/src/main/job)

# Oracle Reader
If you need oraclereader, you should add oraclereader module in parent project pom file.
Then install the right version of ojdbc6 in your local maven repository which defined in oraclereader pom file. 

