package com.tencent.tools.analysis;

/**
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-02 16:24
 **/
public class ByteCodeAnalysisException extends Exception {
    public ByteCodeAnalysisException(String msg) {
        super(msg);
    }

    public ByteCodeAnalysisException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public ByteCodeAnalysisException(Throwable throwable) {
        super(throwable);
    }
}
