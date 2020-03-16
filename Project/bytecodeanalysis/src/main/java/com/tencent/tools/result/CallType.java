package com.tencent.tools.result;

public enum CallType {
    UNKNOWN_TYPE("unknown"),

    NEW_EXPR("初始化对象"),

    METHOD_CALL("方法调用"),

    FIELD_ACCESS("成员变量调用"),

    INSTANCE_OF("instanceof"),

    CHECK_CAST("强制转换"),

    ANEW_ARRAY("数组初始化"),

    TRY_CACHE("异常捕获"),

    GENERIC_INIT("泛型初始化"),
    ;

    private String type;

    CallType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
