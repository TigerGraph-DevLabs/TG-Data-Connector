
# MysqlReader

##  Configuration instructions
     
* Configure a job that synchronizes and extracts data from the mysql database to the local, print on screen..

```
{
    "job": {
        "setting": {
            "speed": {
                 "channel": 3
            },
            "errorLimit": {
                "record": 0,
                "percentage": 0.02
            }
        },
        "content": [
            {
                "reader": {
                    "name": "mysqlreader",
                    "parameter": {
                        "username": "root",
                        "password": "root",
                        "column": [
                            "id",
                            "name"
                        ],
                        "splitPk": "db_id",
                        "connection": [
                            {
                                "table": [
                                    "table"
                                ],
                                "jdbcUrl": [
     "jdbc:mysql://127.0.0.1:3306/database"
                                ]
                            }
                        ]
                    }
                },
               "writer": {
                    "name": "streamwriter",
                    "parameter": {
                        "print":true
                    }
                }
            }
        ]
    }
}

```

###  Parameter Description

* **jdbcUrl**
	* Description: Describes the JDBC connection information to the peer database, using JSON array description, and supports one library to fill in multiple connection addresses. The reason why the JSON array is used to describe the connection information is because the Alibaba Group supports multiple IP detection. If multiple IPs are configured, MysqlReader can detect the connectivity of the IP in turn until a legal IP is selected. If all connections fail, MysqlReader reports an error. Note that jdbcUrl must be included in the connection configuration unit. For the external use of Ali Group, just fill in a JDBC connection in the JSON array.<br />
	* [Reference](http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html). <br />
	* Not null. <br />

* **username**
	* Description: The user name of the data source. <br />
	* Not null. <br />

* **password**
	* Description: The password of the user name specified by the data source <br />
	* Not null. <br />

* **table**
    * Description: The selected table to be synchronized. Use JSON array description, so it supports simultaneous extraction of multiple tables. When configuring multiple tables, the user needs to ensure that the multiple tables have the same schema structure, and MysqlReader does not check whether the tables are the same logical table. Note that the table must be included in the connection configuration unit. <br />
    * Not null. <br />

* **column**
    * Description: The set of column names that need to be synchronized in the configured table, using a JSON array to describe the field information. Users use \* to use all column configurations by default, such as ['\*'].
	* Not null. <br />
	
* **splitPk**
    * Description: When MysqlReader performs data extraction, if splitPk is specified, it means that the user wants to use the field represented by splitPk for data fragmentation. Therefore, DataX will start concurrent tasks for data synchronization, which can greatly improve the efficiency of data synchronization.
      It is recommended that splitPk users use the primary key of the table, because the primary key of the table is usually relatively uniform, so the divided fragments are not prone to data hotspots.
      Currently splitPk only supports segmentation of plastic data, `does not support other types such as floating point, string, date`. If the user specifies other non-supported types, MysqlReader will report an error!
      If splitPk is not filled in, including not providing splitPk or the splitPk value is empty, DataX regards it as using a single channel to synchronize the table data.
	
* **where**
    * Description: Filter conditions, MysqlReader splices SQL according to the specified column, table, and where conditions, and extracts data based on this SQL. In actual business scenarios, the data of the day is often selected for synchronization, and the where condition can be specified as gmt_create> $bizdate. Note: The where condition cannot be specified as limit 10. Limit is not a legal where clause of SQL. <br />
      The where condition can effectively perform business incremental synchronization. If you do not fill in the where statement, including the key or value of where is not provided, DataX will treat it as synchronizing the full amount of data.

* **querySql**	
    * Description: In some business scenarios, where this configuration item is not enough to describe the filtering conditions, users can customize the filtering SQL through this configuration type. When the user configures this item, the DataX system will ignore the table and column configuration types, and directly use the content of this configuration item to filter the data. For example, if you need to synchronize data after multi-table join, use select a,b from table_a join table_b on table_a.id = table_b.id <br />
      `When the user configures querySql, MysqlReader directly ignores the configuration of the table, column, and where conditions. The priority of querySql is greater than the table, column, and where options.
	