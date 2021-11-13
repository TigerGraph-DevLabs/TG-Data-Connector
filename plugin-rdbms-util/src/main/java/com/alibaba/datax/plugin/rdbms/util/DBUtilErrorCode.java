package com.alibaba.datax.plugin.rdbms.util;

import com.alibaba.datax.common.spi.ErrorCode;

//TODO
public enum DBUtilErrorCode implements ErrorCode {
    //连接错误
    MYSQL_CONN_USERPWD_ERROR("MYSQLErrCode-01","The database user name or password is wrong. Please check the account password filled in or contact the DBA to confirm that the account name and password are correct"),
    MYSQL_CONN_IPPORT_ERROR("MYSQLErrCode-02","The IP address or Port of the database service is wrong. Please check the IP address and Port filled in or contact the DBA to confirm whether the IP address and Port are correct.If you are a synchronization center user, please contact the DBA to confirm that the IP and PORT information entered in the IDB is consistent with the current actual information in the database"),
    MYSQL_CONN_DB_ERROR("MYSQLErrCode-03","The database name is wrong. Please check the database instance name or contact the DBA to confirm that the instance exists and is in normal service"),

    ORACLE_CONN_USERPWD_ERROR("ORACLEErrCode-01","The database user name or password is wrong. Please check the account password filled in or contact the DBA to confirm that the account name and password are correct"),
    ORACLE_CONN_IPPORT_ERROR("ORACLEErrCode-02","The IP address or Port of the database service is wrong. Please check the IP address and Port filled in or contact the DBA to confirm whether the IP address and Port are correct.If you are a synchronization center user, please contact the DBA to confirm that the IP and PORT information entered in the IDB is consistent with the current actual information in the database"),
    ORACLE_CONN_DB_ERROR("ORACLEErrCode-03","The database name is wrong. Please check the database instance name or contact the DBA to confirm that the instance exists and is in normal service"),

    //execute query错误
    MYSQL_QUERY_TABLE_NAME_ERROR("MYSQLErrCode-04","The table does not exist. Please check the table name or contact the DBA to confirm the existence of the table"),
    MYSQL_QUERY_SQL_ERROR("MYSQLErrCode-05","SQL statement execution error, check Where condition for spelling or syntax errors"),
    MYSQL_QUERY_COLUMN_ERROR("MYSQLErrCode-06","Column information is wrong. Check if the Column exists. If it is a constant or variable, encase it in English single quotes'"),
    MYSQL_QUERY_SELECT_PRI_ERROR("MYSQLErrCode-07","Error reading table data, because the account does not have the permissions to read table, please contact the DBA to confirm the permissions of this account and authorize"),

    ORACLE_QUERY_TABLE_NAME_ERROR("ORACLEErrCode-04","The table does not exist. Please check the table name or contact the DBA to confirm the existence of the table"),
    ORACLE_QUERY_SQL_ERROR("ORACLEErrCode-05","Column information is wrong. Check if the Column exists. If it is a constant or variable, encase it in English single quotes'"),
    ORACLE_QUERY_SELECT_PRI_ERROR("ORACLEErrCode-06","Error reading table data, because the account does not have the permissions to read table, please contact the DBA to confirm the permissions of this account and authorize"),
    ORACLE_QUERY_SQL_PARSER_ERROR("ORACLEErrCode-07","SQL syntax error, check Where condition for spelling or syntax errors"),

    //PreSql,Post Sql错误
    MYSQL_PRE_SQL_ERROR("MYSQLErrCode-08","Presql syntax error, please check"),
    MYSQL_POST_SQL_ERROR("MYSQLErrCode-09","Presql syntax error, please check"),
    MYSQL_QUERY_SQL_PARSER_ERROR("MYSQLErrCode-10","SQL syntax error, check Where condition for spelling or syntax errors"),

    ORACLE_PRE_SQL_ERROR("ORACLEErrCode-08", "Presql syntax error, please check"),
    ORACLE_POST_SQL_ERROR("ORACLEErrCode-09", "Presql syntax error, please check"),

    //SplitPK 错误
    MYSQL_SPLIT_PK_ERROR("MYSQLErrCode-11","Splitpk error, please check"),
    ORACLE_SPLIT_PK_ERROR("ORACLEErrCode-10","Splitpk error, please check"),

    //Insert,Delete 权限错误
    MYSQL_INSERT_ERROR("MYSQLErrCode-12","The database does not have write permission. Please contact the DBA"),
    MYSQL_DELETE_ERROR("MYSQLErrCode-13","The database does not have delete permission. Please contact the DBA"),
    ORACLE_INSERT_ERROR("ORACLEErrCode-11","The database does not have write permission. Please contact the DBA"),
    ORACLE_DELETE_ERROR("ORACLEErrCode-12","The database does not have delete permission. Please contact the DBA"),

    JDBC_NULL("DBUtilErrorCode-20","The JDBC URL is empty, please check the configuration"),
    JDBC_OB10_ADDRESS_ERROR("DBUtilErrorCode-OB10-01","JDBC OB10 format error, please contact AskDatax"),
    CONF_ERROR("DBUtilErrorCode-00", "Your configuration is wrong."),
    CONN_DB_ERROR("DBUtilErrorCode-10", "Failed to connect to database.Please check your account, password, database name, IP, Port or ask the DBA for help (pay attention to the network environment)."),
    GET_COLUMN_INFO_FAILED("DBUtilErrorCode-01", "Failed to get information about table fields."),
    UNSUPPORTED_TYPE("DBUtilErrorCode-12", "Unsupported database type.Be sure to look at the database types and versions that Datax already supports."),
    COLUMN_SPLIT_ERROR("DBUtilErrorCode-13", "Sharding by primary key failed"),
    SET_SESSION_ERROR("DBUtilErrorCode-14", "Failed to set session."),
    RS_ASYNC_ERROR("DBUtilErrorCode-15", "Failed to get ResultSet Next asynchronously."),

    REQUIRED_VALUE("DBUtilErrorCode-03", "You are missing the parameter values that you must fill in."),
    ILLEGAL_VALUE("DBUtilErrorCode-02", "The parameter value you filled in is not valid."),
    ILLEGAL_SPLIT_PK("DBUtilErrorCode-04", "The primary key column you filled in is not valid. DataAX only supports slicing a primary key into an integer or string type."),
    SPLIT_FAILED_ILLEGAL_SQL("DBUtilErrorCode-15", "The execution of the database SQL failed when Dataax tried to shard the table.Please check your table/splitPk/where configuration and make changes."),
    SQL_EXECUTE_FAIL("DBUtilErrorCode-06", "Perform database Sql failed, please check your configuration of the column/table/where/querySql or seek help from the DBA."),

    // only for reader
    READ_RECORD_FAIL("DBUtilErrorCode-07", "Failed to read database data.Please check your configuration of the column/table/where/querySql or seek help from the DBA."),
    TABLE_QUERYSQL_MIXED("DBUtilErrorCode-08", "Your configuration is messy. You cannot configure both table and querySql at the same time"),
    TABLE_QUERYSQL_MISSING("DBUtilErrorCode-09", "You are not configured correctly. TABLE and QUERYSQL should and should only be configured one."),

    // only for writer
    WRITE_DATA_ERROR("DBUtilErrorCode-05", "Failed to write data to the write table you configured."),
    NO_INSERT_PRIVILEGE("DBUtilErrorCode-11", "The database does not have write permission. Please contact the DBA"),
    NO_DELETE_PRIVILEGE("DBUtilErrorCode-16", "The database does not have DELETE permission. Please contact the DBA"),
    ;

    private final String code;

    private final String description;

    private DBUtilErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }
}
