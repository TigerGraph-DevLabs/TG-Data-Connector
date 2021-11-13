package com.alibaba.datax.plugin.unstructuredstorage.writer;

import com.alibaba.datax.common.spi.ErrorCode;


public enum UnstructuredStorageWriterErrorCode implements ErrorCode {
	ILLEGAL_VALUE("UnstructuredStorageWriter-00", "The parameter value you filled in is not valid."),
	Write_FILE_WITH_CHARSET_ERROR("UnstructuredStorageWriter-01", "The encoding you configured failed to write properly."),
	Write_FILE_IO_ERROR("UnstructuredStorageWriter-02", "The file you configured has an IO exception while writing."),
	RUNTIME_EXCEPTION("UnstructuredStorageWriter-03", "A runtime exception has occurred, please contact us"),
	REQUIRED_VALUE("UnstructuredStorageWriter-04", "You are missing the parameter values that you must fill in."),;

	private final String code;
	private final String description;

	private UnstructuredStorageWriterErrorCode(String code, String description) {
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
