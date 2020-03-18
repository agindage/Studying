package com.tencent.tools.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_ANNOTATION;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_DEPRECATED;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_MODULE;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_OPEN;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC_PHASE;
import static org.objectweb.asm.Opcodes.ACC_STRICT;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ACC_TRANSITIVE;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

/**
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-07 11:18
 **/
public class ByteCodeTools {
    public static String convertByteCodeOwnerToOrigin(String owner) {
        if (null == owner) {
            return null;
        }

        return owner.replaceAll("/", ".");
    }

    /**
     * 功能:把字节码方法描述符转换成java方法描述
     * params
     * methodName - 方法名  例如：“initTTEngine”
     * desc - asm解析成的方法描述符 例如：“(Lcom/tencent/mobileqq/triton/sdk/ITTEngine;Lcom/tencent/mobileqq/triton/sdk/bridge/IJSEngine;)V”
     * return 返回java格式的方法
     * */
    public static String convertMethodDescriptor(String methodName, String desc){
        if (!isMethod(desc)){
            return desc;
        }
        //第一步：提取出desc中括号部分
        Pattern p = Pattern.compile("\\([\\s\\S]*\\)");
        Matcher m = p.matcher(desc);
        m.find();
        String paramsPart = m.group(); //形参部分
        if (TextUtils.isEmptyOrBlank(paramsPart)){
            return desc;
        }
        paramsPart = paramsPart.substring(1, paramsPart.length()-1);//去掉左右括号
        String retPart = desc.substring(m.end(), desc.length()); //返回值部分
        StringBuilder result = new StringBuilder();
        try {
            // 按顺序拼接   返回值 + 方法名 + （ + 参数列表 + ）+ ;
            result.append(convertJniTypeToJavaType(retPart));
            result.append(" ");
            result.append(methodName);
            result.append("(");
            ArrayList<String> paramsItem = splitParams(paramsPart);
            for (int i=0; i<paramsItem.size(); i++){
                if (i > 0){
                    result.append(",");
                }
                result.append(convertJniTypeToJavaType(paramsItem.get(i)));
            }
            result.append(")");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static ArrayList<String> splitParams(String desc) throws Exception {
        if (TextUtils.isEmptyOrBlank(desc)){
            return new ArrayList<String>();
        }
        int index = 0;
        ArrayList<String> allItems = new ArrayList<>();
        StringBuilder item = new StringBuilder();
        while (index < desc.length()){
            char c = desc.charAt(index);
            switch (c){
                case '[':
                    item.append(c);
                    index ++;
                    break;
                case 'I':
                case 'S':
                case 'J':
                case 'Z':
                case 'C':
                case 'B':
                case 'F':
                case 'D':
                case 'V':
                    item.append(c);
                    allItems.add(item.toString());
                    item.delete(0, item.length());
                    index ++;
                    break;
                case 'L':
                    int lastIndex = desc.indexOf(';', index);
                    if (lastIndex < 0){
                        throw new Exception("not support type, " + desc);
                    } else {
                        item.append(desc.substring(index, lastIndex+1));
                        allItems.add(item.toString());
                        item.delete(0, item.length());
                        index = lastIndex + 1;
                    }
                    break;
                default:
                    throw new Exception("not support type, " + desc);
            }
        }
        return allItems;
    }

    /**
     * 功能：是否JNI方法描述符格式
     * params
     *      methodDesc - 输入字符串 例如："(Lcom/tencent/mobileqq/triton/sdk/ITTEngine;Lcom/tencent/mobileqq/triton/sdk/bridge/IJSEngine;)V"
     * return
     *      true - 如果是则返回true；否则为false
     * */
    public static boolean isMethod(String methodDesc){
        final boolean matches = Pattern.matches("^\\([\\s\\S]*\\)[ISJZCBFDVL\\[][\\s\\S]*", methodDesc);
        return matches;//返回true
    }

    /**
     * 功能：把jni类型转成java类型
     * params:
     *     jniType - jni类型字符串
     * return：
     *     对应的java类型
     * */
    public static String convertJniTypeToJavaType(String jniType) throws Exception {
        if (TextUtils.isEmptyOrBlank(jniType)){
            return "";
        }
        boolean isArray = false;
        int arrayLength = 0;
        int index = 0;
        int length = jniType.length();
        //第一步：数组判断
        while ('[' == jniType.charAt(index)){
            index ++;
            arrayLength ++;
        }
        //第二步：根据字符判断类型
        String javaType = "";
        switch (jniType.charAt(index)){
            case 'I':
                javaType = "int";
                break;
            case 'S':
                javaType = "short";
                break;
            case 'J':
                javaType = "long";
                break;
            case 'Z':
                javaType = "boolean";
                break;
            case 'C':
                javaType = "char";
                break;
            case 'B':
                javaType = "byte";
                break;
            case 'F':
                javaType = "float";
                break;
            case 'D':
                javaType = "double";
                break;
            case 'V':
                javaType = "void";
                break;
            case 'L':
                if (!jniType.endsWith(";")){
                    throw new Exception("a Object Type should end with charater \";\" " + jniType);
                }
                javaType = jniType.substring(index+1, length-1);
                break;
            default:
                throw new Exception("not support type, " + jniType);
        }
        StringBuilder result = new StringBuilder();
        result.append(javaType.replace("/", "."));
        for (int i=0; i<arrayLength; i++){
            result.append("[]");
        }
        return result.toString();
    }

    public static String convertAccessFlagToString(int access){
        StringBuilder typeDesc = new StringBuilder();
        while (access > 0){
            if ((access & ACC_PUBLIC) != 0){
                typeDesc.append("public ");
            } else if ((access & ACC_PRIVATE) != 0){
                typeDesc.append("private ");
            } else if ((access & ACC_PROTECTED) != 0){
                typeDesc.append("protected ");
            } else if ((access & ACC_STATIC) != 0){
                typeDesc.append("static ");
            } else if ((access & ACC_FINAL) != 0){
                typeDesc.append("final ");
            } else if ((access & ACC_SUPER) != 0){
            } else if ((access & ACC_SYNCHRONIZED) != 0){
                typeDesc.append("synchronized ");
            } else if ((access & ACC_OPEN) != 0){
            } else if ((access & ACC_TRANSITIVE) != 0){
                typeDesc.append("transitive ");
            } else if ((access & ACC_VOLATILE) != 0){
                typeDesc.append("volatile ");
            } else if ((access & ACC_BRIDGE) != 0){
            } else if ((access & ACC_STATIC_PHASE) != 0){
            } else if ((access & ACC_VARARGS) != 0){
            } else if ((access & ACC_TRANSIENT) != 0){
                typeDesc.append("transient ");
            } else if ((access & ACC_NATIVE) != 0){
                typeDesc.append("native ");
            } else if ((access & ACC_INTERFACE) != 0){
                typeDesc.append("interface ");
            } else if ((access & ACC_ABSTRACT) != 0){
                typeDesc.append("abstract ");
            } else if ((access & ACC_STRICT) != 0){
            } else if ((access & ACC_SYNTHETIC) != 0){
                typeDesc.append("synthetic ");
            } else if ((access & ACC_ANNOTATION) != 0){
            } else if ((access & ACC_ENUM) != 0){
                typeDesc.append("enum ");
            } else if ((access & ACC_MANDATED) != 0){
            } else if ((access & ACC_MODULE) != 0){
            } else if ((access & ACC_DEPRECATED) != 0){
            }
            access = access & (access - 1);
        }
        return typeDesc.toString();
    }
}
