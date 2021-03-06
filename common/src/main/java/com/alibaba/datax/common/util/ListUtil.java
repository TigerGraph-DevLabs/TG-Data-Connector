package com.alibaba.datax.common.util;

import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 提供针对 DataX中使用的 List 较为常见的一些封装。 比如：checkIfValueDuplicate 可以用于检查用户配置的 writer
 * 的列不能重复。makeSureNoValueDuplicate亦然，只是会严格报错。
 */
public final class ListUtil {

    public static boolean checkIfValueDuplicate(List<String> aList,
                                                boolean caseSensitive) {
        if (null == aList || aList.isEmpty()) {
            throw DataXException.asDataXException(CommonErrorCode.CONFIG_ERROR,
                    "The job you provided is incorrectly configured. List cannot be empty.");
        }

        try {
            makeSureNoValueDuplicate(aList, caseSensitive);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public static void makeSureNoValueDuplicate(List<String> aList,
                                                boolean caseSensitive) {
        if (null == aList || aList.isEmpty()) {
            throw new IllegalArgumentException("The job you provided is incorrectly configured. List cannot be empty.");
        }

        if (1 == aList.size()) {
            return;
        } else {
            List<String> list = null;
            if (!caseSensitive) {
                list = valueToLowerCase(aList);
            } else {
                list = new ArrayList<String>(aList);
            }

            Collections.sort(list);

            for (int i = 0, len = list.size() - 1; i < len; i++) {
                if (list.get(i).equals(list.get(i + 1))) {
                    throw DataXException
                            .asDataXException(
                                    CommonErrorCode.CONFIG_ERROR,
                                    String.format(
                                            "String:[%s] is not allowed to appear repeatedly in the list :[%s].",
                                            list.get(i),
                                            StringUtils.join(aList, ",")));
                }
            }
        }
    }

    public static boolean checkIfBInA(List<String> aList, List<String> bList,
                                      boolean caseSensitive) {
        if (null == aList || aList.isEmpty() || null == bList
                || bList.isEmpty()) {
            throw new IllegalArgumentException("The job you provided is incorrectly configured. List cannot be empty.");
        }

        try {
            makeSureBInA(aList, bList, caseSensitive);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void makeSureBInA(List<String> aList, List<String> bList,
                                    boolean caseSensitive) {
        if (null == aList || aList.isEmpty() || null == bList
                || bList.isEmpty()) {
            throw new IllegalArgumentException("The job you provided is incorrectly configured. List cannot be empty.");
        }

        List<String> all = null;
        List<String> part = null;

        if (!caseSensitive) {
            all = valueToLowerCase(aList);
            part = valueToLowerCase(bList);
        } else {
            all = new ArrayList<String>(aList);
            part = new ArrayList<String>(bList);
        }

        for (String oneValue : part) {
            if (!all.contains(oneValue)) {
                throw DataXException
                        .asDataXException(
                                CommonErrorCode.CONFIG_ERROR,
                                String.format(
                                        "The job configuration information you provided is incorrect. String:[%s] does not exist in the list :[%s].",
                                        oneValue, StringUtils.join(aList, ",")));
            }
        }

    }

    public static boolean checkIfValueSame(List<Boolean> aList) {
        if (null == aList || aList.isEmpty()) {
            throw new IllegalArgumentException("The job you provided is incorrectly configured. List cannot be empty.");
        }

        if (1 == aList.size()) {
            return true;
        } else {
            Boolean firstValue = aList.get(0);
            for (int i = 1, len = aList.size(); i < len; i++) {
                if (firstValue.booleanValue() != aList.get(i).booleanValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static List<String> valueToLowerCase(List<String> aList) {
        if (null == aList || aList.isEmpty()) {
            throw new IllegalArgumentException("The job you provided is incorrectly configured. List cannot be empty.");
        }
        List<String> result = new ArrayList<String>(aList.size());
        for (String oneValue : aList) {
            result.add(null != oneValue ? oneValue.toLowerCase() : null);
        }

        return result;
    }
}
