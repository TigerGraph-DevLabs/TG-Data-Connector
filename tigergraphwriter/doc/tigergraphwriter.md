# DataX TigerGraphWriter

## Configuration instructions
* Configure a job that synchronizes and extracts data from streamreader to tigergraph.


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
                    "table": ["job loadJob","job loadJob"],
                    "sep": ",",
                    "filename": ["user","test"],
                    "eol": "\n",
                    "debug": "2"
                  }
                }
            }
        ]
    }
}

```


###  Parameter Description

* **jdbcUrl**
	* tigergraph jdbcurl
 	* Not null <br />

* **username**
	* Description: The user name of the data source. <br />
	* Not null. <br />

* **password**
	* Description: The password of the user name specified by the data source <br />
	* Not null. <br />

* **table**
	* TigerGraph loading job name.
	* Not null. <br />

* **writeMode**
	* TigerGraph loading job write mode.
	* Not null. <br />
	
* **graph**
	* Insert graph name in TigerGraph loading job
	* Not null. <br />

* **sep**
	* File separator defined in loading job.
	* Not null. <br />
	
* **filename**
	* File name defined in loading job.
	* Not null. <br />

* **eol**
	* File eol defined in loading job.
	* Not null. <br />

#### Support SSL
* Support SSL by tg-jdbc-driver. [Details](https://github.com/tigergraph/ecosys/tree/master/tools/etl/tg-jdbc-driver)
  [Encrypting Connections](https://docs.tigergraph.com/tigergraph-server/3.3/security/encrypting-connections)

* **trustStore**
	* TrustStore file path.
	* Default null. <br />
	
* **trustStorePassword**
	* TrustStore file password.
	* Default: "changeit". <br />
	
* **trustStoreType**
	* TrustStore file type.
	* Default: "JKS". <br />

* **keyStore**
	* KeyStore file path.
	* Default null. <br />
	
* **keyStorePassword**
	* KeyStore file password.
	* Default: "changeit". <br />
	
* **keyStoreType**
	* KeyStore file type.
	* Default: "JKS". <br />

