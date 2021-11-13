package com.alibaba.datax.plugin.reader.gcsreader;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * @author beichen
 * @Date : 2021/11/4 15:01
 */
public enum GcsReaderErrorCode implements ErrorCode {
    PEM_NOT_FIND_ERROR("GcsReader-00", "Authentication json path not exist."),
    BUCKET_NOT_FIND_ERROR("GcsReader-01", "Bucket not exist."),
    BLOB_NOT_FIND_ERROR("GcsReader-02", "Blob not exist."),
    FIELD_DELIMITER_NOT_FIND_ERROR("GcsReader-03", "Field delimiter not find."),
    BLOB_NOT_EXIST_ERROR("GcsReader-04", "Blob not exist"),
    ILLEGAL_VALUE("GcsReader-03", "Illegal parameter."),
    REQUIRED_COLUMNS_VALUE("GcsReader-05", "Columns not specified."),
    SKIP_HEADER_NOT_FIND("GcsReader-06","Skip header not find." );

    private final String code;
    private final String description;

    private GcsReaderErrorCode(String code, String description) {
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
