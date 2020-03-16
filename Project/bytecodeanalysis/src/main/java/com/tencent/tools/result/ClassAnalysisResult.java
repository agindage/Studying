package com.tencent.tools.result;

import com.tencent.tools.common.GsonTools;
import com.tencent.tools.common.TextUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 类结构解析结果
 *
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-07 09:17
 **/
public class ClassAnalysisResult implements Serializable {
    /**
     * 模块名
     */
    private String moduleName = "";

    /**
     * 类名
     */
    private String name = "";

    /**
     * 父类
     */
    private String superName = "";

    /**
     * 继承的接口
     */
    private List<String> interfaces = new ArrayList<>();

    /**
     * 源文件名
     */
    private String sourceFile = "";

    /**
     * signature：泛型中才会将该属性编译进字节码文件，JDK 1.5才加入，除了方法参数和返回值，还包含了泛型信息；
     */
    private String signature = "";

    private List<MethodAnalysisResult> methodAnalysisResults = new ArrayList<>();
    private List<FieldAnalysisResult> fieldAnalysisResults = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void addInterface(String interfaceName) {
        if (null == interfaces) {
            interfaces = new ArrayList<>();
        }
        interfaces.add(interfaceName);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<MethodAnalysisResult> getMethodAnalysisResults() {
        return methodAnalysisResults;
    }

    public void setMethodAnalysisResults(List<MethodAnalysisResult> methodAnalysisResults) {
        this.methodAnalysisResults = methodAnalysisResults;
    }

    public void addMethodAnalysisResult(MethodAnalysisResult methodAnalysisResult) {
        if (null == methodAnalysisResults) {
            methodAnalysisResults = new ArrayList<>();
        }

        methodAnalysisResults.add(methodAnalysisResult);
    }

    public List<FieldAnalysisResult> getFieldAnalysisResults() {
        return fieldAnalysisResults;
    }

    public void setFieldAnalysisResults(List<FieldAnalysisResult> fieldAnalysisResults) {
        this.fieldAnalysisResults = fieldAnalysisResults;
    }

    public void addFieldAnalysisResult(FieldAnalysisResult fieldAnalysisResult) {
        if (null == fieldAnalysisResults) {
            fieldAnalysisResults = new ArrayList<>();
        }

        fieldAnalysisResults.add(fieldAnalysisResult);
    }

    @Override
    public String toString() {
        try {
            return GsonTools.getJsonOriginStr(this, true);
        } catch (Exception e) {
            return super.toString();
        }
    }

    /**
     * 使用name生成md5字符串，用于数据库索引(直接字符串拼接)
     */
    public String getMd5Index() {
        return DigestUtils.md5Hex(name).substring(8, 24);
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}