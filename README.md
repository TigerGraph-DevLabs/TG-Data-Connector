We support transport data to tigergraph from mysql, sqlserver, oracle and GCS.

Connector based on Datax.
# DataX Introduction
##### Reference：[DataX-Introduction](https://github.com/alibaba/DataX/blob/master/introduction.md)

# System Requirements
- Linux
- Java 8+
- Maven 3.5.4+
- Python 2.6.x| Python 3.x, both supported
- Install tg-java-driver jar to your local maven repository.
   1. Get tg-java-driver-1.2.jar
    - Compile the source code https://github.com/tigergraph/ecosys/tree/master/tools/etl/tg-jdbc-driver
    - Or get it from : [SF2TGconfig](https://github.com/TigerGraph-DevLabs/TG-Snowflake-Connector/tree/main/SF2TGconfig)
    2. Install  to maven repository
       ```bash
       mvn install:install-file -Dfile=/{location}/tg-java-driver-1.2.jar -DgroupId=com.tigergraph -DartifactId=tg-java-driver -Dversion=1.2 -Dpackaging=jar
       ``` 


# Package
Download source code then running following command.
```shell script
mvn clean package -DskipTests assembly:assembly 
```

## Package structure
├── datax <br />
│   ├── bin <br />
│   ├── conf <br />
│   ├── job <br />
│   ├── lib <br />
│   ├── log <br />
│   ├── log_perf <br />
│   ├── plugin <br />
│   └── script <br />

# Execute job 
### python 2 : 
    ``` shell
    $ cd  {YOUR_DATAX_HOME}/bin
    $ python datax.py {YOUR_JOB.json}
    ```
### python 3 :
    ``` shell
    $ cd  {YOUR_DATAX_HOME}/bin/python3
    $ python datax.py {YOUR_JOB.json}
    ```

# Config job
Reference to resources/plugin_job_template.json in each reader or writer.
 * [Mysql Reader Job Config](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/blob/main/mysqlreader/doc/mysqlreader.md)
 * [Oracle Reader Job Config](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/blob/main/oraclereader/doc/oraclereader.md)
 * [SqlServer Reader Job Config](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/blob/main/sqlserverreader/doc/sqlserverreader.md)
 * [GCS Reader Job Config](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/blob/main/gcsreader/doc/gcsreader.md)
 * [TigerGraph Writer Job Config](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/blob/main/tigergraphwriter/doc/tigergraphwriter.md)
 
[Job Examples](https://github.com/TigerGraph-DevLabs/TG-Data-Connector/tree/main/core/src/main/job)

# Oracle Reader
If you need oraclereader, you should add oraclereader module in parent project pom file.
Then install the right version of ojdbc6 in your local maven repository which defined in oraclereader pom file. 

