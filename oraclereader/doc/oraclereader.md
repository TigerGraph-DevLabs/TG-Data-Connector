
# OracleReader 

## Configuration instructions
* Configure a job that synchronizes and extracts data from the Gcs database to the local.

```
{
    "job": {
        "setting": {
            "speed": {
            //设置传输速度 byte/s 尽量逼近这个速度但是不高于它.
            // channel 表示通道数量，byte表示通道速度，如果单通道速度1MB，配置byte为1048576表示一个channel
                 "byte": 1048576
            },
            //出错限制
                "errorLimit": {
                //先选择record
                "record": 0,
                //百分比  1表示100%
                "percentage": 0.02
            }
        },
        "content": [
            {
                "reader": {
                    "name": "oraclereader",
                    "parameter": {
                        // 数据库连接用户名
                        "username": "root",
                        // 数据库连接密码
                        "password": "root",
                        "column": [
                            "id","name"
                        ],
                        //切分主键
                        "splitPk": "db_id",
                        "connection": [
                            {
                                "table": [
                                    "table"
                                ],
                                "jdbcUrl": [
     "jdbc:oracle:thin:@[HOST_NAME]:PORT:[DATABASE_NAME]"
                                ]
                            }
                        ]
                    }
                },
               "writer": {
                  //writer类型
                    "name": "streamwriter",
                  // 是否打印内容
                    "parameter": {
                        "print": true
                    }
                }
            }
        ]
    }
}

```