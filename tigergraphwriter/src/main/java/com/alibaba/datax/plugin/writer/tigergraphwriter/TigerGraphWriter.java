package com.alibaba.datax.plugin.writer.tigergraphwriter;

import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.datax.plugin.rdbms.writer.CommonRdbmsWriter;
import com.alibaba.datax.plugin.rdbms.writer.Constant;
import com.alibaba.datax.plugin.rdbms.writer.Key;
import com.tigergraph.jdbc.Driver;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//TODO writeProxy
public class TigerGraphWriter extends Writer {
    private static final DataBaseType DATABASE_TYPE = DataBaseType.TigerGraph;

    public static class Job extends Writer.Job {
        private Configuration originalConfig = null;
        private CommonRdbmsWriter.Job commonRdbmsWriterJob;

        @Override
        public void preCheck() {
            this.init();
            this.commonRdbmsWriterJob.writerPreCheck(this.originalConfig, DATABASE_TYPE);
        }

        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();
            this.commonRdbmsWriterJob = new CommonRdbmsWriter.Job(DATABASE_TYPE);
        }

        @Override
        public void prepare() {
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> split = new ArrayList<>();

            List<Object> tables = this.originalConfig.getList("table");
            List<Object> files = this.originalConfig.getList(TKey.FILE_NAME);

            for (int i = 0; i < mandatoryNumber; i++) {
                Configuration configuration = this.originalConfig.clone();
                configuration.set(TKey.TABLE, tables.get(i));
                configuration.set(TKey.FILE_NAME, files.get(i));
                split.add(configuration);
            }

            return split;
        }

        public static List<Configuration> doSplit(Configuration simplifiedConf,
                                                  int adviceNumber) {

            List<Configuration> splitResultConfigs = new ArrayList<Configuration>();


            //由于在之前的  master prepare 中已经把 table,jdbcUrl 提取出来，所以这里处理十分简单
            for (int j = 0; j < adviceNumber; j++) {
                splitResultConfigs.add(simplifiedConf.clone());
            }

            return splitResultConfigs;

        }

        // 一般来说，是需要推迟到 task 中进行post 的执行（单表情况例外）
        @Override
        public void post() {
            //this.commonRdbmsWriterJob.post(this.originalConfig);
        }

        @Override
        public void destroy() {
            //this.commonRdbmsWriterJob.destroy(this.originalConfig);
        }

    }

    public static class Task extends Writer.Task {
        private Configuration writerSliceConfig;
        private CommonRdbmsWriter.Task commonRdbmsWriterTask;

        private String username;
        private String password;
        private String jdbcUrl;
        private String table;
        private String writeRecordSql;

        private DataBaseType dataBaseType;

        private int batchSize;
        private int batchByteSize;

        private String trustStore;
        private String trustStorePassword;
        private String trustStoreType;

        private Properties properties = new Properties();

        public Connection getConnection() throws SQLException {
            com.tigergraph.jdbc.Driver driver = new Driver();

            return driver.connect(this.jdbcUrl, properties);
        }

        @Override
        public void init() {
            this.writerSliceConfig = super.getPluginJobConf();
            this.commonRdbmsWriterTask = new CommonRdbmsWriter.Task(DATABASE_TYPE);

            this.username = writerSliceConfig.getString(TKey.USERNAME);
            this.password = writerSliceConfig.getString(TKey.PASSWORD);
            this.jdbcUrl = writerSliceConfig.getString(TKey.JDBC_URL);
            this.table = writerSliceConfig.getString(TKey.TABLE);

            this.dataBaseType = DataBaseType.TigerGraph;
            this.batchSize = writerSliceConfig.getInt(Key.BATCH_SIZE, Constant.DEFAULT_BATCH_SIZE);
            this.batchByteSize = writerSliceConfig.getInt(Key.BATCH_BYTE_SIZE, Constant.DEFAULT_BATCH_BYTE_SIZE);

            String filename = writerSliceConfig.getString(TKey.FILE_NAME, TConstant.DEFAULT_FILE_NAME);
            String sep = writerSliceConfig.getString(TKey.SEP, TConstant.DEFAULT_SEP);
            String eol = writerSliceConfig.getString(TKey.EOL, TConstant.DEFAULT_EOL);
            String debug = writerSliceConfig.getString(TKey.DEBUG, TConstant.DEFAULT_DEBUG);
            String token = writerSliceConfig.getString(TKey.TOKEN, TConstant.DEFAULT_TOKEN);
            String graph = writerSliceConfig.getString(TKey.GRAPH);
            properties.setProperty(TKey.USERNAME, username);
            properties.setProperty(TKey.PASSWORD, password);
            properties.setProperty(TKey.FILE_NAME, filename);
            properties.setProperty(TKey.DEBUG, debug);
            properties.setProperty(TKey.SEP, sep);
            properties.setProperty(TKey.EOL, eol);
            properties.setProperty(TKey.GRAPH, graph);
            if (!TConstant.DEFAULT_TOKEN.equalsIgnoreCase(token)) {
                properties.setProperty(TKey.TOKEN, token);
            }

            String trustStore = writerSliceConfig.getString("trustStore", "");
            String trustStorePassword = writerSliceConfig.getString("trustStorePassword", "");
            String trustStoreType = writerSliceConfig.getString("trustStoreType", "");
            if (!"".equalsIgnoreCase(trustStore)) {
                properties.setProperty("trustStore", trustStore);
            }
            if (!"".equalsIgnoreCase(trustStorePassword)) {
                properties.setProperty("trustStorePassword", trustStorePassword);
            }
            if (!"".equalsIgnoreCase(trustStoreType)) {
                properties.setProperty("trustStoreType", trustStoreType);
            }

        }

        @Override
        public void prepare() {
        }

        //TODO 改用连接池，确保每次获取的连接都是可用的（注意：连接可能需要每次都初始化其 session）
        public void startWrite(RecordReceiver recordReceiver) {
            try (Connection connection = getConnection()) {
                startWriteWithConnection(recordReceiver, this.writerSliceConfig, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void startWriteWithConnection(RecordReceiver recordReceiver, Configuration taskPluginCollector, Connection connection) {
            // 写数据库的SQL语句
            calcWriteRecordSql();

            List<Record> writeBuffer = new ArrayList<Record>(this.batchSize);
            int bufferBytes = 0;
            try {
                Record record;
                while ((record = recordReceiver.getFromReader()) != null) {
                    writeBuffer.add(record);
                    bufferBytes += record.getMemorySize();

                    if (writeBuffer.size() >= batchSize || bufferBytes >= batchByteSize) {
                        doBatchInsert(connection, writeBuffer);
                        writeBuffer.clear();
                        bufferBytes = 0;
                    }
                }

                if (!writeBuffer.isEmpty()) {
                    doBatchInsert(connection, writeBuffer);
                    writeBuffer.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw DataXException.asDataXException(
                        DBUtilErrorCode.WRITE_DATA_ERROR, e);
            } finally {
                writeBuffer.clear();
                DBUtil.closeDBResources(null, null, connection);
            }
        }

        private void calcWriteRecordSql() {
            //query = "INSERT INTO job load_pagerank(line) VALUES(?)";
            this.writeRecordSql = "INSERT INTO " + this.table + "(line) VALUES(?)";
        }

        protected void doBatchInsert(Connection connection, List<Record> buffer)
                throws SQLException {
            PreparedStatement preparedStatement = null;
            try {
                connection.setAutoCommit(false);
                preparedStatement = connection
                        .prepareStatement(this.writeRecordSql);

                // 兼容format & dataFormat
                String dateFormat = this.writerSliceConfig.getString(TKey.DATE_FORMAT, TConstant.DEFAULT_DATE_FORMAT);
                DateFormat dateParse = null; // warn: 可能不兼容
                if (StringUtils.isNotBlank(dateFormat)) {
                    dateParse = new SimpleDateFormat(dateFormat);
                }

                for (Record record : buffer) {
                    preparedStatement = Record2StringWriterUtil.filePreparedStatement(preparedStatement,
                            record,
                            dateParse,
                            "", (Character) this.properties.get(TKey.SEP));
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                throw DataXException.asDataXException(
                        DBUtilErrorCode.WRITE_DATA_ERROR, e);
            } finally {
                DBUtil.closeDBResources(preparedStatement, null);
            }
        }

        @Override
        public void post() {
            //this.commonRdbmsWriterTask.post(this.writerSliceConfig);
        }

        @Override
        public void destroy() {
            //this.commonRdbmsWriterTask.destroy(this.writerSliceConfig);
        }

        @Override
        public boolean supportFailOver() {
            String writeMode = writerSliceConfig.getString(Key.WRITE_MODE);
            return "replace".equalsIgnoreCase(writeMode);
        }

    }


}
