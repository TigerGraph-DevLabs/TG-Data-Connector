# DataX TigerGraphWriter


---


## 1 快速介绍

TigerGraphWriter 插件实现了写入数据到 TigerGraph 主库的目的表的功能。在底层实现上， TigerGraphWriter 通过 JDBC 连接远程 TigerGraph 数据库，并执行相应的 loading job 语句将数据写入 TigerGraph。

TigerGraphWriter 面向ETL开发工程师，他们使用 TigerGraphWriter 从数仓导入数据到 TigerGraph。同时 MysqlWriter 亦可以作为数据迁移工具为DBA等用户提供服务。


## 2 实现原理

MysqlWriter 通过 DataX 框架获取 Reader 生成的协议数据，根据你配置的 `writeMode` 生成


* `insert into...`(当主键/唯一性索引冲突时会写不进去冲突的行)

##### 或者

* `replace into...`(没有遇到主键/唯一性索引冲突时，与 insert into 行为一致，冲突时会用新行替换原有行所有字段) 的语句写入数据到 Mysql。出于性能考虑，采用了 `PreparedStatement + Batch`，并且设置了：`rewriteBatchedStatements=true`，将数据缓冲到线程上下文 Buffer 中，当 Buffer 累计到预定阈值时，才发起写入请求。

<br />

    注意：目的表所在数据库必须是主库才能写入数据；整个任务至少需要具备 insert/replace into...的权限，是否需要其他权限，取决于你任务配置中在 preSql 和 postSql 中指定的语句。


## 3 功能说明

### 3.1 配置样例

* 这里使用一份从内存产生到 Mysql 导入的数据。

```json
{
    "job": {
        "setting": {
            "speed": {
                "channel": 1
            }
        },
        "content": [
            {
                 "reader": {
                    "name": "streamreader",
                    "parameter": {
                        "column" : [
                            {
                                "value": "DataX",
                                "type": "string"
                            },
                            {
                                "value": 19880808,
                                "type": "long"
                            },
                            {
                                "value": "1988-08-08 08:08:08",
                                "type": "date"
                            },
                            {
                                "value": true,
                                "type": "bool"
                            },
                            {
                                "value": "test",
                                "type": "bytes"
                            }
                        ],
                        "sliceRecordCount": 1000
                    }
                },
                "writer": {
                  "name": "tigergraphwriter",
                  "parameter": {
                    "username":"tigergraph",
                    "password":"tigergraph",
                    "token":"tokenKey",
                    "jdbcUrl": "jdbc:tg:http://192.168.100.21:14240",
                    "writeMode": "insert",
                    "graph": "test_con",
                    "table": [
                      "job loadPeople2"
                    ],
                    "sep": ",",
                    "filename": "f",
                    "eol": "\n",
                    "debug": "2"
                  }
                }
            }
        ]
    }
}

```


### 3.2 参数说明

* **jdbcUrl**

	* 描述：目的数据库的 JDBC 连接信息。作业运行时，DataX 会在你提供的 jdbcUrl 后面追加如下属性：yearIsDateType=false&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true

               注意：tigergraph官方的jdbcurl


 	* 必选：是 <br />

	* 默认值：无 <br />

* **username**

	* 描述：目的数据库的用户名 <br />

	* 必选：是 <br />

	* 默认值：无 <br />

* **password**

	* 描述：目的数据库的密码 <br />

	* 必选：是 <br />

	* 默认值：无 <br />

* **table**

	* 描述：目的表的表名称。table表示的是loading job名字。抽取几张表，与reader中的table需要一致

	* 必选：是 <br />

	* 默认值：无 <br />

* **batchSize**

	* 描述：一次性批量提交的记录数大小，该值可以极大减少DataX与TigerGraph的网络交互次数，并提升整体吞吐量。但是该值设置过大可能会造成DataX运行进程OOM情况。<br />

	* 必选：否 <br />

	* 默认值：1024 <br />
