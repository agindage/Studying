package com.tencent.tools.common;

import java.util.function.Function;

/**
 * @program: ByteCodeAnalysisTool
 * @description:
 * @author: caesarzhli
 * @create: 2020-02-21 14:21
 **/
public class ProtoBuilder {
    /**
     * Sets the given value, using the given setter, only if the value is not {@code null}.
     *
     * @return this proto builder, for chaining calls.
     */
    public static <T, BuilderType> void set(Function<T,BuilderType> setter, T value){
        if (value != null){
            setter.apply(value);
        }
    }
}
