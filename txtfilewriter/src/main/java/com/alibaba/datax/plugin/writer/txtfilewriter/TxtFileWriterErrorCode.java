package com.alibaba.datax.plugin.writer.txtfilewriter;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * Created by haiwei.luo on 14-9-17.
 */
public enum TxtFileWriterErrorCode implements ErrorCode {

    CONFIG_INVALID_EXCEPTION("TxtFileWriter-00", "Your parameters are incorrectly configured."),
    REQUIRED_VALUE("TxtFileWriter-01", "You are missing the parameter values that you must fill in."),
    ILLEGAL_VALUE("TxtFileWriter-02", "The parameter value you filled in is not valid."),
    Write_FILE_ERROR("TxtFileWriter-03", "The object file you configured is abnormal on write."),
    Write_FILE_IO_ERROR("TxtFileWriter-04", "The file you configured has an IO exception while writing."),
    SECURITY_NOT_ENOUGH("TxtFileWriter-05", "You do not have permission to perform the appropriate file write operation.");

    private final String code;
    private final String description;

    private TxtFileWriterErrorCode(String code, String description) {
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
        return String.format("Code:[%s], Description:[%s].", this.code,
                this.description);
    }

}
