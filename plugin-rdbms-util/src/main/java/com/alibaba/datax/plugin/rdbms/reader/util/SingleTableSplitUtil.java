package com.alibaba.datax.plugin.rdbms.reader.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.reader.Constant;
import com.alibaba.datax.plugin.rdbms.reader.Key;
import com.alibaba.datax.plugin.rdbms.util.*;
import com.alibaba.fastjson.JSON;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SingleTableSplitUtil {
    private static final Logger LOG = LoggerFactory
            .getLogger(SingleTableSplitUtil.class);

    public static DataBaseType DATABASE_TYPE;

    private SingleTableSplitUtil() {
    }

    public static List<Configuration> splitSingleTable(
            Configuration configuration, int adviceNum) {
        List<Configuration> pluginParams = new ArrayList<Configuration>();
        List<String> rangeList;
        String splitPkName = configuration.getString(Key.SPLIT_PK);
        String column = configuration.getString(Key.COLUMN);
        String table = configuration.getString(Key.TABLE);
        String where = configuration.getString(Key.WHERE, null);
        boolean hasWhere = StringUtils.isNotBlank(where);

        //String splitMode = configuration.getString(Key.SPLIT_MODE, "");
        //if (Constant.SPLIT_MODE_RANDOMSAMPLE.equals(splitMode) && DATABASE_TYPE == DataBaseType.Oracle) {
        if (DATABASE_TYPE == DataBaseType.Oracle) {
            rangeList = genSplitSqlForOracle(splitPkName, table, where,
                    configuration, adviceNum);
            // warn: mysql etc to be added...
        } else {
            Pair<Object, Object> minMaxPK = getPkRange(configuration);
            if (null == minMaxPK) {
                throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                        "Shard table failed by shard primary key.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
            }

            configuration.set(Key.QUERY_SQL, buildQuerySql(column, table, where));
            if (null == minMaxPK.getLeft() || null == minMaxPK.getRight()) {
                // 切分后获取到的start/end 有 Null 的情况
                pluginParams.add(configuration);
                return pluginParams;
            }

            boolean isStringType = Constant.PK_TYPE_STRING.equals(configuration
                    .getString(Constant.PK_TYPE));
            boolean isLongType = Constant.PK_TYPE_LONG.equals(configuration
                    .getString(Constant.PK_TYPE));


            if (isStringType) {
                rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                        String.valueOf(minMaxPK.getLeft()),
                        String.valueOf(minMaxPK.getRight()), adviceNum,
                        splitPkName, "'", DATABASE_TYPE);
            } else if (isLongType) {
                rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                        new BigInteger(minMaxPK.getLeft().toString()),
                        new BigInteger(minMaxPK.getRight().toString()),
                        adviceNum, splitPkName);
            } else {
                throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                        "The Split Primary Key (SplitPK) type you configured, Dataax, is not supported.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
            }
        }
        String tempQuerySql;
        List<String> allQuerySql = new ArrayList<String>();

        if (null != rangeList && !rangeList.isEmpty()) {
            for (String range : rangeList) {
                Configuration tempConfig = configuration.clone();

                tempQuerySql = buildQuerySql(column, table, where)
                        + (hasWhere ? " and " : " where ") + range;

                allQuerySql.add(tempQuerySql);
                tempConfig.set(Key.QUERY_SQL, tempQuerySql);
                pluginParams.add(tempConfig);
            }
        } else {
            //pluginParams.add(configuration); // this is wrong for new & old split
            Configuration tempConfig = configuration.clone();
            tempQuerySql = buildQuerySql(column, table, where)
                    + (hasWhere ? " and " : " where ")
                    + String.format(" %s IS NOT NULL", splitPkName);
            allQuerySql.add(tempQuerySql);
            tempConfig.set(Key.QUERY_SQL, tempQuerySql);
            pluginParams.add(tempConfig);
        }

        // deal pk is null
        Configuration tempConfig = configuration.clone();
        tempQuerySql = buildQuerySql(column, table, where)
                + (hasWhere ? " and " : " where ")
                + String.format(" %s IS NULL", splitPkName);

        allQuerySql.add(tempQuerySql);

        LOG.info("After split(), allQuerySql=[\n{}\n].",
                StringUtils.join(allQuerySql, "\n"));

        tempConfig.set(Key.QUERY_SQL, tempQuerySql);
        pluginParams.add(tempConfig);

        return pluginParams;
    }

    public static String buildQuerySql(String column, String table,
                                          String where) {
        String querySql;

        if (StringUtils.isBlank(where)) {
            querySql = String.format(Constant.QUERY_SQL_TEMPLATE_WITHOUT_WHERE,
                    column, table);
        } else {
            querySql = String.format(Constant.QUERY_SQL_TEMPLATE, column,
                    table, where);
        }

        return querySql;
    }

    @SuppressWarnings("resource")
    private static Pair<Object, Object> getPkRange(Configuration configuration) {
        String pkRangeSQL = genPKRangeSQL(configuration);

        int fetchSize = configuration.getInt(Constant.FETCH_SIZE);
        String jdbcURL = configuration.getString(Key.JDBC_URL);
        String username = configuration.getString(Key.USERNAME);
        String password = configuration.getString(Key.PASSWORD);
        String table = configuration.getString(Key.TABLE);

        Connection conn = DBUtil.getConnection(DATABASE_TYPE, jdbcURL, username, password);
        Pair<Object, Object> minMaxPK = checkSplitPk(conn, pkRangeSQL, fetchSize, table, username, configuration);
        DBUtil.closeDBResources(null, null, conn);
        return minMaxPK;
    }

    public static void precheckSplitPk(Connection conn, String pkRangeSQL, int fetchSize,
                                                       String table, String username) {
        Pair<Object, Object> minMaxPK = checkSplitPk(conn, pkRangeSQL, fetchSize, table, username, null);
        if (null == minMaxPK) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "Shard table failed by shard primary key.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
        }
    }

    /**
     * 检测splitPk的配置是否正确。
     * configuration为null, 是precheck的逻辑，不需要回写PK_TYPE到configuration中
     *
     */
    private static Pair<Object, Object> checkSplitPk(Connection conn, String pkRangeSQL, int fetchSize,  String table,
                                                     String username, Configuration configuration) {
        LOG.info("split pk [sql={}] is running... ", pkRangeSQL);
        ResultSet rs = null;
        Pair<Object, Object> minMaxPK = null;
        try {
            try {
                rs = DBUtil.query(conn, pkRangeSQL, fetchSize);
            }catch (Exception e) {
                throw RdbmsException.asQueryException(DATABASE_TYPE, e, pkRangeSQL,table,username);
            }
            ResultSetMetaData rsMetaData = rs.getMetaData();
            if (isPKTypeValid(rsMetaData)) {
                if (isStringType(rsMetaData.getColumnType(1))) {
                    if(configuration != null) {
                        configuration
                                .set(Constant.PK_TYPE, Constant.PK_TYPE_STRING);
                    }
                    while (DBUtil.asyncResultSetNext(rs)) {
                        minMaxPK = new ImmutablePair<Object, Object>(
                                rs.getString(1), rs.getString(2));
                    }
                } else if (isLongType(rsMetaData.getColumnType(1))) {
                    if(configuration != null) {
                        configuration.set(Constant.PK_TYPE, Constant.PK_TYPE_LONG);
                    }

                    while (DBUtil.asyncResultSetNext(rs)) {
                        minMaxPK = new ImmutablePair<Object, Object>(
                                rs.getString(1), rs.getString(2));

                        // check: string shouldn't contain '.', for oracle
                        String minMax = rs.getString(1) + rs.getString(2);
                        if (StringUtils.contains(minMax, '.')) {
                            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                                    "The DataX splitter primary key (splitPk) you configured is incorrect.Because the Split Primary Key (SplitPK) type you configured, Datax, is not supported.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
                        }
                    }
                } else {
                    throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                            "The DataX splitter primary key (splitPk) you configured is incorrect.Because the Split Primary Key (SplitPK) type you configured, Datax, is not supported.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
                }
            } else {
                throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                        "The DataX splitter primary key (splitPk) you configured is incorrect.Because the Split Primary Key (SplitPK) type you configured, Datax, is not supported.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
            }
        } catch(DataXException e) {
            throw e;
        } catch (Exception e) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK, "An error occurred in the DataX attempt to shard the table.Please check your configuration and make changes.", e);
        } finally {
            DBUtil.closeDBResources(rs, null, null);
        }

        return minMaxPK;
    }

    private static boolean isPKTypeValid(ResultSetMetaData rsMetaData) {
        boolean ret = false;
        try {
            int minType = rsMetaData.getColumnType(1);
            int maxType = rsMetaData.getColumnType(2);

            boolean isNumberType = isLongType(minType);

            boolean isStringType = isStringType(minType);

            if (minType == maxType && (isNumberType || isStringType)) {
                ret = true;
            }
        } catch (Exception e) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "DATAX failed to obtain SplitPK field type.This error is usually the result of an underlying system exception.Please contact Want Want: AskDatax or DBA processing.");
        }
        return ret;
    }

    // warn: Types.NUMERIC is used for oracle! because oracle use NUMBER to
    // store INT, SMALLINT, INTEGER etc, and only oracle need to concern
    // Types.NUMERIC
    private static boolean isLongType(int type) {
        boolean isValidLongType = type == Types.BIGINT || type == Types.INTEGER
                || type == Types.SMALLINT || type == Types.TINYINT;

        switch (SingleTableSplitUtil.DATABASE_TYPE) {
            case Oracle:
                isValidLongType |= type == Types.NUMERIC;
                break;
            default:
                break;
        }
        return isValidLongType;
    }

    private static boolean isStringType(int type) {
        return type == Types.CHAR || type == Types.NCHAR
                || type == Types.VARCHAR || type == Types.LONGVARCHAR
                || type == Types.NVARCHAR;
    }

    private static String genPKRangeSQL(Configuration configuration) {

        String splitPK = configuration.getString(Key.SPLIT_PK).trim();
        String table = configuration.getString(Key.TABLE).trim();
        String where = configuration.getString(Key.WHERE, null);
        return genPKSql(splitPK,table,where);
    }

    public static String genPKSql(String splitPK, String table, String where){

        String minMaxTemplate = "SELECT MIN(%s),MAX(%s) FROM %s";
        String pkRangeSQL = String.format(minMaxTemplate, splitPK, splitPK,
                table);
        if (StringUtils.isNotBlank(where)) {
            pkRangeSQL = String.format("%s WHERE (%s AND %s IS NOT NULL)",
                    pkRangeSQL, where, splitPK);
        }
        return pkRangeSQL;
    }

    /**
     * support Number and String split
     * */
    public static List<String> genSplitSqlForOracle(String splitPK,
            String table, String where, Configuration configuration,
            int adviceNum) {
        if (adviceNum < 1) {
            throw new IllegalArgumentException(String.format(
                    "The number of slices must not be less than 1. Here :adviceNum=[%s].", adviceNum));
        } else if (adviceNum == 1) {
            return null;
        }
        String whereSql = String.format("%s IS NOT NULL", splitPK);
        if (StringUtils.isNotBlank(where)) {
            whereSql = String.format(" WHERE (%s) AND (%s) ", whereSql, where);
        } else {
            whereSql = String.format(" WHERE (%s) ", whereSql);
        }
        Double percentage = configuration.getDouble(Key.SAMPLE_PERCENTAGE, 0.1);
        String sampleSqlTemplate = "SELECT * FROM ( SELECT %s FROM %s SAMPLE (%s) %s ORDER BY DBMS_RANDOM.VALUE) WHERE ROWNUM <= %s ORDER by %s ASC";
        String splitSql = String.format(sampleSqlTemplate, splitPK, table,
                percentage, whereSql, adviceNum, splitPK);

        int fetchSize = configuration.getInt(Constant.FETCH_SIZE, 32);
        String jdbcURL = configuration.getString(Key.JDBC_URL);
        String username = configuration.getString(Key.USERNAME);
        String password = configuration.getString(Key.PASSWORD);
        Connection conn = DBUtil.getConnection(DATABASE_TYPE, jdbcURL,
                username, password);
        LOG.info("split pk [sql={}] is running... ", splitSql);
        ResultSet rs = null;
        List<Pair<Object, Integer>> splitedRange = new ArrayList<Pair<Object, Integer>>();
        try {
            try {
                rs = DBUtil.query(conn, splitSql, fetchSize);
            } catch (Exception e) {
                throw RdbmsException.asQueryException(DATABASE_TYPE, e,
                        splitSql, table, username);
            }
            if (configuration != null) {
                configuration
                        .set(Constant.PK_TYPE, Constant.PK_TYPE_MONTECARLO);
            }
            ResultSetMetaData rsMetaData = rs.getMetaData();
            while (DBUtil.asyncResultSetNext(rs)) {
                ImmutablePair<Object, Integer> eachPoint = new ImmutablePair<Object, Integer>(
                        rs.getObject(1), rsMetaData.getColumnType(1));
                splitedRange.add(eachPoint);
            }
        } catch (DataXException e) {
            throw e;
        } catch (Exception e) {
            throw DataXException.asDataXException(
                    DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "An error occurred in the DataX attempt to shard the table.Please check your configuration and make changes.", e);
        } finally {
            DBUtil.closeDBResources(rs, null, null);
        }
        LOG.debug(JSON.toJSONString(splitedRange));
        List<String> rangeSql = new ArrayList<String>();
        int splitedRangeSize = splitedRange.size();
        // warn: splitedRangeSize may be 0 or 1，切分规则为IS NULL以及 IS NOT NULL
        // demo: Parameter rangeResult can not be null and its length can not <2. detail:rangeResult=[24999930].
        if (splitedRangeSize >= 2) {
            // warn: oracle Number is long type here
            if (isLongType(splitedRange.get(0).getRight())) {
                BigInteger[] integerPoints = new BigInteger[splitedRange.size()];
                for (int i = 0; i < splitedRangeSize; i++) {
                    integerPoints[i] = new BigInteger(splitedRange.get(i)
                            .getLeft().toString());
                }
                rangeSql.addAll(RdbmsRangeSplitWrap.wrapRange(integerPoints,
                        splitPK));
                // its ok if splitedRangeSize is 1
                rangeSql.add(RdbmsRangeSplitWrap.wrapFirstLastPoint(
                        integerPoints[0], integerPoints[splitedRangeSize - 1],
                        splitPK));
            } else if (isStringType(splitedRange.get(0).getRight())) {
                // warn: treated as string type
                String[] stringPoints = new String[splitedRange.size()];
                for (int i = 0; i < splitedRangeSize; i++) {
                    stringPoints[i] = new String(splitedRange.get(i).getLeft()
                            .toString());
                }
                rangeSql.addAll(RdbmsRangeSplitWrap.wrapRange(stringPoints,
                        splitPK, "'", DATABASE_TYPE));
                // its ok if splitedRangeSize is 1
                rangeSql.add(RdbmsRangeSplitWrap.wrapFirstLastPoint(
                        stringPoints[0], stringPoints[splitedRangeSize - 1],
                        splitPK, "'", DATABASE_TYPE));
            } else {
                throw DataXException
                        .asDataXException(
                                DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                                "The DataX splitter primary key (splitPk) you configured is incorrect.Because the Split Primary Key (SplitPK) type you configured, Datax, is not supported.DataAX only supports shards of one primary key and of type integer or string.Try using a different shard primary key or contact the DBA for processing.");
            }
        }
        return rangeSql;
    }
}
