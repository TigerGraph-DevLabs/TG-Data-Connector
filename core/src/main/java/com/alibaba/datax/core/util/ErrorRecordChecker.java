package com.alibaba.datax.core.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.util.container.CoreConstant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查任务是否到达错误记录限制。有检查条数（recordLimit）和百分比(percentageLimit)两种方式。
 * 1. errorRecord表示出错条数不能大于限制数，当超过时任务失败。比如errorRecord为0表示不容许任何脏数据。
 * 2. errorPercentage表示出错比例，在任务结束时校验。
 * 3. errorRecord优先级高于errorPercentage。
 */
public final class ErrorRecordChecker {
    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorRecordChecker.class);

    private Long recordLimit;
    private Double percentageLimit;

    public ErrorRecordChecker(Configuration configuration) {
        this(configuration.getLong(CoreConstant.DATAX_JOB_SETTING_ERRORLIMIT_RECORD),
                configuration.getDouble(CoreConstant.DATAX_JOB_SETTING_ERRORLIMIT_PERCENT));
    }

    public ErrorRecordChecker(Long rec, Double percentage) {
        recordLimit = rec;
        percentageLimit = percentage;

        if (percentageLimit != null) {
            Validate.isTrue(0.0 <= percentageLimit && percentageLimit <= 1.0,
                    "The dirty data percentage limit should be between [0.0, 1.0]");
        }

        if (recordLimit != null) {
            Validate.isTrue(recordLimit >= 0,
                    "The number of dirty data bars should now be non-negative integers");

            // errorRecord优先级高于errorPercentage.
            percentageLimit = null;
        }
    }

    public void checkRecordLimit(Communication communication) {
        if (recordLimit == null) {
            return;
        }

        long errorNumber = CommunicationTool.getTotalErrorRecords(communication);
        if (recordLimit < errorNumber) {
            LOG.debug(
                    String.format("Error-limit set to %d, error count check.",
                            recordLimit));
            throw DataXException.asDataXException(
                    FrameworkErrorCode.PLUGIN_DIRTY_DATA_LIMIT_EXCEED,
                    String.format("The dirty data count check fails, the limit is [%d], but in fact [%d] is caught.",
                            recordLimit, errorNumber));
        }
    }

    public void checkPercentageLimit(Communication communication) {
        if (percentageLimit == null) {
            return;
        }
        LOG.debug(String.format(
                "Error-limit set to %f, error percent check.", percentageLimit));

        long total = CommunicationTool.getTotalReadRecords(communication);
        long error = CommunicationTool.getTotalErrorRecords(communication);

        if (total > 0 && ((double) error / (double) total) > percentageLimit) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.PLUGIN_DIRTY_DATA_LIMIT_EXCEED,
                    String.format("Dirty data percentage check fails, limit is [%f], but actually catch [%f].",
                            percentageLimit, ((double) error / (double) total)));
        }
    }
}
