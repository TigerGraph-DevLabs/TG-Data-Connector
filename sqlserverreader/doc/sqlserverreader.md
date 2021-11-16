
# SqlServerReader 插件文档

___


## Configuration instructions

* Configure a job that synchronizes and extracts data from the Gcs database to the local.

```
{
    "job": {
        "setting": {
            "speed": {
                 "byte": 1048576
            }
        },
        "content": [
            {
                "reader": {
                    "name": "sqlserverreader",
                    "parameter": {
                        // 数据库连接用户名
                        "username": "root",
                        // 数据库连接密码
                        "password": "root",
                        "column": [
                            "id"
                        ],
                        "splitPk": "db_id",
                        "connection": [
                            {
                                "table": [
                                    "table"
                                ],
                                "jdbcUrl": [
                                "jdbc:sqlserver://localhost:3433;DatabaseName=dbname"
                                ]
                            }
                        ]
                    }
                },
               "writer": {
                    "name": "streamwriter",
                    "parameter": {
                        "print": true,
                        "encoding": "UTF-8"
                    }
                }
            }
        ]
    }
}
```

* 配置一个自定义SQL的数据库同步任务到本地内容的作业：

```
{
    "job": {
        "setting": {
            "speed": 1048576
        },
        "content": [
            {
                "reader": {
                    "name": "sqlserverreader",
                    "parameter": {
                        "username": "root",
                        "password": "root",
                        "where": "",
                        "connection": [
                            {
                                "querySql": [
                                    "select db_id,on_line_flag from db_info where db_id < 10;"
                                ],
                                "jdbcUrl": [
                                    "jdbc:sqlserver://bad_ip:3433;DatabaseName=dbname",
                                    "jdbc:sqlserver://127.0.0.1:bad_port;DatabaseName=dbname",
                                    "jdbc:sqlserver://127.0.0.1:3306;DatabaseName=dbname"
                                ]
                            }
                        ]
                    }
                },
                "writer": {
                    "name": "streamwriter",
                    "parameter": {
                        "visible": false,
                        "encoding": "UTF-8"
                    }
                }
            }
        ]
    }
}
```
