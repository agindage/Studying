package com.tencent.tools.common;

/**
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-07 11:12
 **/
public class TextUtils {
    public static boolean isEmptyOrBlank(String msg) {
        return msg == null || msg.trim().isEmpty();
    }

    public static boolean isEmpty(String msg) {
        return msg == null || msg.isEmpty();
    }
}
