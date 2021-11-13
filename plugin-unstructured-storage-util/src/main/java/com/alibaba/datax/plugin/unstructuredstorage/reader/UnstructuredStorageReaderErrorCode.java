package com.alibaba.datax.plugin.unstructuredstorage.reader;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * Created by haiwei.luo on 14-9-20.
 */
public enum UnstructuredStorageReaderErrorCode implements ErrorCode {
	CONFIG_INVALID_EXCEPTION("UnstructuredStorageReader-00", "Your parameters are incorrectly configured."),
	NOT_SUPPORT_TYPE("UnstructuredStorageReader-01","The column type you configured is not currently supported."),
	REQUIRED_VALUE("UnstructuredStorageReader-02", "You are missing the parameter values that you must fill in."),
	ILLEGAL_VALUE("UnstructuredStorageReader-03", "The parameter value you filled in is not valid."),
	MIXED_INDEX_VALUE("UnstructuredStorageReader-04", "Your column information configuration includes both index and value."),
	NO_INDEX_VALUE("UnstructuredStorageReader-05","You specified the configuration column information, but did not fill in the corresponding index,value."),
	FILE_NOT_EXISTS("UnstructuredStorageReader-06", "The source path you configured does not exist."),
	OPEN_FILE_WITH_CHARSET_ERROR("UnstructuredStorageReader-07", "The encoding you configured does not match the actual storage encoding."),
	OPEN_FILE_ERROR("UnstructuredStorageReader-08", "The source you configured is abnormal when opened. It is recommended that you check whether the source has hidden entities, pipeline files and other special files."),
	READ_FILE_IO_ERROR("UnstructuredStorageReader-09", "The file you configured has an IO exception while reading."),
	SECURITY_NOT_ENOUGH("UnstructuredStorageReader-10", "You lack permission to perform the corresponding file read operation."),
	RUNTIME_EXCEPTION("UnstructuredStorageReader-11", "A runtime exception has occurred, please contact us");

	private final String code;
	private final String description;

	private UnstructuredStorageReaderErrorCode(String code, String description) {
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
