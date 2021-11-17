
# GCSReader introduction

## 1 Brief
Support csv、txt
GCSReader read data from Google Cloud Storage by using java client which connect Google Cloud Storage remotely.


## 2 Google Cloud Storage Credentials
Authenticating as a service account,details ref: https://cloud.google.com/docs/authentication/production 

## 3 Configuration instructions

* Configure a job that synchronizes and extracts data from the GCS to the local, print on screen.
```
{
    "job": {
        "setting": {
            "speed": {
                "byte":10485760
            },
            "errorLimit": {
                "record": 0,
                "percentage": 0.02
            }
        },
        "content": [
            {
                "reader": {
                    "name": "gcsreader",
                    "parameter": {
                        "credentials": "/home/ubuntu/gcs/key/gcs2tg-0e3531a0b06c.json",
                        "bucketName": "tgtest",
                        "objectName": ["user.csv","test500m.csv"],
                        "columnIndex" : [1,2,3,4],
                        "fileType": "csv",
                        "encoding": "UTF-8",
                        "fieldDelimiter": ",",
                        "skipHeader": false
                    }
                },
                "writer": {
                    "name": "tigergraphwriter",
                    "parameter": {
                        "username": "tigergraph",
                        "password": "tigergraph",
                        "jdbcUrl": "jdbc:tg:http://127.0.0.1:14240",
                        "writeMode": "insert",
                        "graph": "Social_network",
                        "table": ["job loadUser","job loadUser"],
                        "sep": ",",
                        "filename": "user",
                        "eol": "\n"
                    }
                }
            }
        ]
    },
    "core": {
        "transport": {
            "channel": {
                "speed": {
                    "byte": 1048576
                }
            }
        }
    }
}

```

###  Parameter Description

* **credentials**

	Authenticating KEY_PATH. Not null.

* **bucketName**

	GCS Storage bucket name. Not null.

* **objectName**

	* GCS Storage object name. Not null.
	* Multiple files with the same field can be successfully read.
	* If you use tigergraphwriter, the order of objects corresponds to the order of tables

* **columnIndex**

    Columns need to read in file. If columnIndex is not configured, all columns are read by default.

* **fileType**

	* File type. Not null.
	
* **encoding**

	* File encoding. Not null.
	
* **fieldDelimiter**

    * Field delimiter. Not null.

* **skipHeader**	

    * Whether skip header when reading file. Not null.
	


