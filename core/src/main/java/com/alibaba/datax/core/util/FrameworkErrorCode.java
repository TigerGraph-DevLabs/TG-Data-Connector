package com.alibaba.datax.core.util;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * TODO: 根据现有日志数据分析各类错误，进行细化。
 *
 * <p>请不要格式化本类代码</p>
 */
public enum FrameworkErrorCode implements ErrorCode {

	INSTALL_ERROR("Framework-00", "DATAX engine installation error, please contact your operation to solve."),
	ARGUMENT_ERROR("Framework-01", "The DataX engine is running wrong. This problem is usually caused by an internal programming error. Please contact the DataX development team to resolve it."),
	RUNTIME_ERROR("Framework-02", "The DataX engine ran in error. See the error diagnosis information at the end of the DataAX run for details."),
	CONFIG_ERROR("Framework-03", "DataX engine configuration error. This problem is usually caused by a DATAX installation error. Please contact your operations for resolution."),
    SECRET_ERROR("Framework-04", "DATAX engine encryption and decryption error. This problem is usually caused by DATAX key configuration error. Please contact your operation to solve it."),
    HOOK_LOAD_ERROR("Framework-05", "An error occurred loading an external Hook, usually due to a Datax installation"),
    HOOK_FAIL_ERROR("Framework-06", "Error executing external Hook"),

    PLUGIN_INSTALL_ERROR("Framework-10", "Datax plugin installation error. This problem is usually caused by a DataX installation error. Please contact your operations for resolution."),
    PLUGIN_NOT_FOUND("Framework-11", "The DataX plugin is not configured correctly. This problem is usually caused by a DataAX installation error. Please contact your operations for resolution."),
    PLUGIN_INIT_ERROR("Framework-12", "DataX plugin initialization error. This problem is usually caused by a DataX installation error. Please contact your operations to resolve it."),
    PLUGIN_RUNTIME_ERROR("Framework-13", "The DataX plug-in failed at runtime. See the error diagnosis information at the end of the DataAX run for details."),
    PLUGIN_DIRTY_DATA_LIMIT_EXCEED("Framework-14", "DATAX transits more dirty data than the user expects. This error is usually caused by the presence of more business dirty data in the source side data. Please carefully check the dirty data log information reported by DATAX, or you can appropriately increase the dirty data threshold."),
    PLUGIN_SPLIT_ERROR("Framework-15", "The problem is usually caused by programming errors in each of the Datax plug-ins. Please contact the Datax development team to resolve the problem"),
    KILL_JOB_TIMEOUT_ERROR("Framework-16", "KILL task timeout, please contact PE to solve"),
    START_TASKGROUP_ERROR("Framework-17", "TaskGroup failed to start, please contact the Datax development team to resolve"),
    CALL_DATAX_SERVICE_FAILED("Framework-18", "Error requesting DataX Service."),
    CALL_REMOTE_FAILED("Framework-19", "Remote call failed"),
    KILLED_EXIT_VALUE("Framework-143", "Job received the Kill command.");

    private final String code;

    private final String description;

    private FrameworkErrorCode(String code, String description) {
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

    /**
     * 通过 "Framework-143" 来标示 任务是 Killed 状态
     */
    public int toExitValue() {
        if (this == FrameworkErrorCode.KILLED_EXIT_VALUE) {
            return 143;
        } else {
            return 1;
        }
    }

}
