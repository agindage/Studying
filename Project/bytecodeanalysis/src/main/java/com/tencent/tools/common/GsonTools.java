package com.tencent.tools.common;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-06 21:06
 **/
public class GsonTools {
    /**
     * 读取json文件到对象中
     * @param f 文件对象
     * @param typeOfT 需要转换的对象type
     * @return 对象
     */
    public static <T> T loadJson(File f, Type typeOfT) {
        return loadJson(f, null, typeOfT);
    }

    /**
     * 读取json文件到对象中
     * @param f 文件
     * @param classOfT 对象class
     * @return 解析的对象
     */
    public static <T> T loadJson(File f, Class<T> classOfT) {
        return loadJson(f, classOfT, null);
    }

    private static <T> T loadJson(File f, Class<T> classOfT, Type typeOfT) {
        if (null == classOfT && null == typeOfT) {
            return null;
        }
        if (f.exists()) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                if (null == typeOfT) {
                    return gson.fromJson(br, classOfT);
                } else {
                    return gson.fromJson(br, typeOfT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 读取json字符串到对象中
     * @param str 字符串
     * @param typeOfT 需要转换的对象type
     * @return 对象
     */
    public static <T> T loadJsonStr(String str, Type typeOfT) {
        return loadJsonStr(str, null, typeOfT);
    }

    /**
     * 读取json字符串到对象中
     * @param str 字符串
     * @param classOfT 对象class
     * @return 解析的对象
     */
    public static <T> T loadJsonStr(String str, Class<T> classOfT) {
        return loadJsonStr(str, classOfT, null);
    }

    private static <T> T loadJsonStr(String str, Class<T> classOfT, Type typeOfT) {
        if (null == classOfT && null == typeOfT) {
            return null;
        }

        if (null == str) {
            return null;
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        if (null == typeOfT) {
            return gson.fromJson(str, classOfT);
        } else {
            return gson.fromJson(str, typeOfT);
        }
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @return 对象转换的json字符串
     */
    public static String getJsonOriginStr(Object obj) throws Exception {
        return getJsonOriginStr(obj, false, null);
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @param pretty 是否优化展示
     * @return json字符串
     * @throws Exception
     */
    public static String getJsonOriginStr(Object obj, boolean pretty) throws Exception {
        return getJsonOriginStr(obj, null, pretty, null);
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @param type 对象类型
     * @return json字符串
     * @throws Exception
     */
    public static String getJsonOriginStr(Object obj, Type type) throws Exception {
        return getJsonOriginStr(obj, type, false, null);
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @param exclusionStrategy 需要过滤的字段
     * @return json字符串
     * @throws Exception
     */
    public static String getJsonOriginStr(Object obj, ExclusionStrategy exclusionStrategy) throws Exception {
        return getJsonOriginStr(obj, false, exclusionStrategy);
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @param pretty 是否优化展示
     * @param exclusionStrategy 需要过滤的字段
     * @return json字符串
     * @throws Exception
     */
    public static String getJsonOriginStr(Object obj, boolean pretty, ExclusionStrategy exclusionStrategy) throws Exception {
        return getJsonOriginStr(obj, null, pretty, exclusionStrategy);
    }

    /**
     * 获取对象的json字符串
     * @param obj 对象
     * @param type 对象类型
     * @param exclusionStrategy 需要过滤的字段
     * @return json字符串
     * @throws Exception
     */
    public static String getJsonOriginStr(Object obj, Type type, ExclusionStrategy exclusionStrategy) throws Exception {
        return getJsonOriginStr(obj, type, false, exclusionStrategy);
    }

    private static String getJsonOriginStr(Object obj, Type type, boolean pretty, ExclusionStrategy exclusionStrategy) throws Exception {
        try (Writer writer = new StringWriter()) {
            GsonBuilder builder = genBuilder(pretty, exclusionStrategy);
            Gson gson = builder.create();
            if (null == type) {
                gson.toJson(obj, writer);
            } else {
                gson.toJson(obj, type, writer);
            }
            return writer.toString();
        }
    }

    private static GsonBuilder genBuilder(boolean pretty, ExclusionStrategy exclusionStrategy) {
        GsonBuilder builder = new GsonBuilder();
        if (pretty) {
            builder.setPrettyPrinting();
        }

        if (null != exclusionStrategy) {
            builder.setExclusionStrategies(exclusionStrategy);
        }
        builder.disableHtmlEscaping();
        return builder;
    }

    /**
     * 保存json数据到文件
     *
     * @param filePath 文件路径
     * @param obj      保存的json数据
     * @throws Exception
     */
    public static void saveJsonToFile(String filePath, Object obj, boolean pretty, ExclusionStrategy exclusionStrategy) throws Exception {
        File parentFile = new File(filePath).getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                //System.err.println("mkdirs failed! may multiprocess create");
            }
        }
        try (Writer writer = new FileWriter(filePath)) {
            GsonBuilder builder = genBuilder(pretty, exclusionStrategy);
            Gson gson = builder.create();
            gson.toJson(obj, writer);
        }
    }
}
