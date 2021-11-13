package com.alibaba.datax.plugin.writer.tigergraphwriter;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.DateColumn;
import com.alibaba.datax.common.element.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class Record2StringWriterUtil {
    private Record2StringWriterUtil() {

    }

    private static final Logger LOG = LoggerFactory
            .getLogger(Record2StringWriterUtil.class);

    public static PreparedStatement filePreparedStatement(PreparedStatement preparedStatement,
                                                          Record record,
                                                          DateFormat dateParse,
                                                          String nullFormat,
                                                          char separator) throws SQLException {

        List<String> strings = transportOneRecord(record, nullFormat, dateParse);
        String parameter = listToString(strings, separator);
        preparedStatement.setString(1, parameter);

        return preparedStatement;
    }

    public static String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    /**
     * 异常表示脏数据
     */
    public static List<String> transportOneRecord(Record record, String nullFormat,
                                                  DateFormat dateParse) {
        // warn: default is null
        if (null == nullFormat) {
            nullFormat = "";
        }
        List<String> splitedRows = new ArrayList<String>();
        try {
            int recordLength = record.getColumnNumber();
            if (0 != recordLength) {
                Column column;
                for (int i = 0; i < recordLength; i++) {
                    column = record.getColumn(i);
                    if (null != column.getRawData()) {
                        boolean isDateColumn = column instanceof DateColumn;
                        if (!isDateColumn) {
                            splitedRows.add(column.asString());
                        } else {
                            if (null != dateParse) {
                                splitedRows.add(dateParse.format(column
                                        .asDate()));
                            } else {
                                splitedRows.add(column.asString());
                            }
                        }
                    } else {
                        // warn: it's all ok if nullFormat is null
                        splitedRows.add(nullFormat);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("format error.");
        }
        return splitedRows;
    }
}
