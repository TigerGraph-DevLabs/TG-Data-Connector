package com.alibaba.datax.common.exception;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 *
 */
public enum CommonErrorCode implements ErrorCode {

    CONFIG_ERROR("Common-00", "The configuration file you provided contains error information. Please check your job configuration."),
    CONVERT_NOT_SUPPORT("Common-01", "Synchronized data business dirty data situation, data type conversion error."),
    CONVERT_OVER_FLOW("Common-02", "Synchronized data dirty data situation, data type conversion overflow."),
    RETRY_FAIL("Common-10", "Method calls still fail multiple times."),
    RUNTIME_ERROR("Common-11", "Internal call error at runtime."),
    HOOK_INTERNAL_ERROR("Common-12", "Hook running error ."),
    SHUT_DOWN_TASK("Common-20", "Task received the shutdown instruction to prepare for the failover"),
    WAIT_TIME_EXCEED("Common-21", "Waiting time out of range"),
    TASK_HUNG_EXPIRED("Common-22", "Task hung, Expired");

    private final String code;

    private final String describe;

    private CommonErrorCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.describe;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Describe:[%s]", this.code,
                this.describe);
    }

}
