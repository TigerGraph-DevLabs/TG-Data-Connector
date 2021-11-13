package com.alibaba.datax.plugin.reader.gcsreader;

import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.unstructuredstorage.reader.ColumnEntry;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.csvreader.CsvReader;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

/**
 * @author beichen
 * @Date : 2021/11/4 11:23
 * GCS read file support type
 *     "CSV"
 */
public class GcsReader extends Reader {
    /**
     * Methods in Job only run once, methods in Task will be executed by the framework to start multiple Task threads in parallel.
     * <p/>
     * Reader execute order:
     * <pre>
     * Job init-->prepare-->split
     *
     * Task init-->prepare-->startRead-->post-->destroy
     * Task init-->prepare-->startRead-->post-->destroy
     *
     * Job post-->destroy
     * </pre>
     */
    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration readerOriginConfig = null;
        private Storage storage = null;
        private List<String> objectNames = null;

        @Override
        public void init() {
            LOG.info("init() begin...");
            this.readerOriginConfig = super.getPluginJobConf();
            String objString = ((JSONArray) this.readerOriginConfig.get(Key.OBJECT_NAME)).toJSONString();
            objectNames = JSONObject.parseArray(objString, String.class);

            this.validate();
            LOG.info("init() ok and end...");
        }

        public void validate() {
            this.readerOriginConfig.getNecessaryValue(Key.CREADENTIALS,
                    GcsReaderErrorCode.PEM_NOT_FIND_ERROR);
            this.readerOriginConfig.getNecessaryValue(Key.BUCKET_NAME,
                    GcsReaderErrorCode.BUCKET_NOT_FIND_ERROR);

            this.readerOriginConfig.getNecessaryValue(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER,
                    GcsReaderErrorCode.FIELD_DELIMITER_NOT_FIND_ERROR);

            this.readerOriginConfig.getNecessaryValue(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.SKIP_HEADER,
                    GcsReaderErrorCode.SKIP_HEADER_NOT_FIND);

            if (null == objectNames || objectNames.isEmpty()) {
                throw DataXException.asDataXException(GcsReaderErrorCode.BLOB_NOT_FIND_ERROR,
                        String.format("You provided the configuration file incorrectly. [%s] is a required parameter. It is not allowed to be blank or blank.", "objectName"));
            }
        }

        @Override
        public void prepare() {
            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream((String) readerOriginConfig.get(Key.CREADENTIALS)))
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

                for(String objectName : this.objectNames) {
                    Blob blob = this.storage.get(BlobId.of((String) readerOriginConfig.get(Key.BUCKET_NAME), objectName));
                    if(! blob.exists()){
                        String message = "Verify whether blob exists in your GCS";
                        LOG.error(message);
                        throw DataXException.asDataXException(GcsReaderErrorCode.BLOB_NOT_EXIST_ERROR, message);
                    }
                }
            } catch (IOException e) {
                String message = "Authentication json not exist. Authenticating as a service account refer to : https://cloud.google.com/docs/authentication/production";
                LOG.error(message);
                throw DataXException.asDataXException(GcsReaderErrorCode.PEM_NOT_FIND_ERROR, message);
            }
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            LOG.info("split() begin...");
            List<Configuration> readerSplitConfigs = new ArrayList<Configuration>();

            int splitNumber = this.objectNames.size();

            List<List<String>> splitedSourceFiles = this.splitSourceFiles(new ArrayList<String>(this.objectNames), splitNumber);

            for (List<String> objects : splitedSourceFiles) {
                Configuration splitedConfig = this.readerOriginConfig.clone();
                splitedConfig.set("sourceFiles", objects);
                readerSplitConfigs.add(splitedConfig);
            }

            return readerSplitConfigs;

        }

        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList, int adviceNumber) {
            List<List<T>> splitedList = new ArrayList<List<T>>();
            int averageLength = sourceList.size() / adviceNumber;
            averageLength = averageLength == 0 ? 1 : averageLength;

            for (int begin = 0, end = 0; begin < sourceList.size(); begin = end) {
                end = begin + averageLength;
                if (end > sourceList.size()) {
                    end = sourceList.size();
                }
                splitedList.add(sourceList.subList(begin, end));
            }
            return splitedList;
        }

        @Override
        public void destroy() {

        }
    }

    public static class Task extends Reader.Task {
        private static Logger LOG = LoggerFactory.getLogger(Reader.Task.class);

        private Configuration taskConfig = null;
        private Storage storage = null;
        private String encoding = null;
        private String fieldDelimiter = null;
        private String bucketName = null;
        private List<String> objectNames = null;
        private List<Integer> indexs = null;
        private Boolean skipHeader = null;

        @Override
        public void init() {
            this.taskConfig = super.getPluginJobConf();
            String objString = ((JSONArray) this.taskConfig.get(Key.OBJECT_NAME)).toJSONString();
            objectNames = JSONObject.parseArray(objString, String.class);
            this.encoding = (String) this.taskConfig.get(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING);
            this.bucketName = (String) this.taskConfig.get(Key.BUCKET_NAME);
            this.fieldDelimiter = (String) this.taskConfig.get(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER);
            this.skipHeader = (Boolean) this.taskConfig.get(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.SKIP_HEADER);
            String indexString = ((JSONArray) this.taskConfig.get(Key.COLUMN_INDEX)).toJSONString();
            this.indexs = JSONObject.parseArray(indexString, Integer.class);
        }

        @Override
        public void prepare() {
            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream((String) taskConfig.get("credentials")))
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            } catch (IOException e) {
                String message = "Authentication json not exist. Authenticating as a service account refer to : https://cloud.google.com/docs/authentication/production";
                LOG.error(message);
                throw DataXException.asDataXException(GcsReaderErrorCode.PEM_NOT_FIND_ERROR, message);
            }
        }

        @Override
        public void startRead(RecordSender recordSender) {

            LOG.info("read start");
            for (String objectName : this.objectNames) {
                LOG.info(String.format("reading file : [%s]", objectName));
                ReadChannel reader = storage.reader(this.bucketName, objectName);

                BufferedReader br = new BufferedReader(Channels.newReader(reader, this.encoding));

                doReadFromStream(br, this.taskConfig,
                        recordSender, this.getTaskPluginCollector());

                if(recordSender != null){
                    recordSender.flush();
                }

                LOG.info(String.format("Finished read file : [%s]", objectName));

            }

            LOG.info("end read source files...");

        }

        public void doReadFromStream(BufferedReader reader, Configuration readerSliceConfig, RecordSender recordSender,
                                     TaskPluginCollector taskPluginCollector) {

            if (null != this.fieldDelimiter && 1 != this.fieldDelimiter.length()) {
                throw DataXException.asDataXException(
                        GcsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("Only support single char as delimiterInStr, yours : [%s]", this.fieldDelimiter));
            }

            String nullFormat = readerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.NULL_FORMAT);

            CsvReader csvReader  = null;

            // every line logic
            try {
                if (skipHeader) {
                    LOG.info("reader readLine : ");

                    String fetchLine = reader.readLine();
                    LOG.info(String.format("Header line %s has been skiped.",
                            fetchLine));
                }

                csvReader = new CsvReader(reader);
                LOG.info("get csvReader : " + csvReader.toString());

                csvReader.setDelimiter(this.fieldDelimiter.charAt(0));

                String[] parseRows;
                while ((parseRows = splitBufferedReader(csvReader)) != null) {
                    transportOneRecord(recordSender, parseRows, nullFormat, taskPluginCollector);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw DataXException.asDataXException(
                        UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION,
                        String.format("Runtime exception : %s", e.getMessage()), e);
            } finally {
                csvReader.close();
                IOUtils.closeQuietly(reader);
            }
        }

        public static String[] splitBufferedReader(CsvReader csvReader)
                throws IOException {
            String[] splitedResult = null;
            if (csvReader.readRecord()) {
                splitedResult = csvReader.getValues();
            }
            return splitedResult;
        }

        public static List<ColumnEntry> getListColumnEntry(Configuration configuration) {
            List<JSONObject> lists = configuration.getList(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN, JSONObject.class);
            if (lists == null) {
                return null;
            }
            List<ColumnEntry> result = new ArrayList<ColumnEntry>();
            for (final JSONObject object : lists) {
                result.add(JSON.parseObject(object.toJSONString(),
                        ColumnEntry.class));
            }
            return result;
        }

        public Record transportOneRecord(RecordSender recordSender, String[] sourceLine,
                                                String nullFormat, TaskPluginCollector taskPluginCollector) {
            Record record = recordSender.createRecord();
            Column columnGenerated = null;

            if (null ==  this.indexs ||  this.indexs.size() == 0) {
                for (String columnValue : sourceLine) {
                    // not equalsIgnoreCase, it's all ok if nullFormat is null
                    if (columnValue.equals(nullFormat)) {
                        columnGenerated = new StringColumn(null);
                    } else {
                        columnGenerated = new StringColumn(columnValue);
                    }
                    record.addColumn(columnGenerated);
                }
                recordSender.sendToWriter(record);
            } else {
                try {
                    for (Integer index : this.indexs) {
                        String columnValue = sourceLine[index];
                        columnGenerated = new StringColumn(columnValue);
                        record.addColumn(columnGenerated);
                    }
                    recordSender.sendToWriter(record);
                } catch (IllegalArgumentException iae) {
                    taskPluginCollector
                            .collectDirtyRecord(record, iae.getMessage());
                } catch (IndexOutOfBoundsException ioe) {
                    taskPluginCollector
                            .collectDirtyRecord(record, ioe.getMessage());
                } catch (Exception e) {
                    if (e instanceof DataXException) {
                        throw (DataXException) e;
                    }
                    // Collect dirty record.
                    taskPluginCollector.collectDirtyRecord(record, e.getMessage());
                }
            }
            return record;
        }


        @Override
        public void destroy() {

        }
    }
}
