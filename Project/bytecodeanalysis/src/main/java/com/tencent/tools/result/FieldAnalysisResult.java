package com.tencent.tools.result;

import com.tencent.tools.common.GsonTools;
import com.tencent.tools.common.TextUtils;
import java.io.Serializable;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 成员变量检测结果
 *
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-07 09:39
 **/
public class FieldAnalysisResult implements Serializable {
    /**
     * 调用类名
     */
    private String owner = "";
    /**
     * 调用方法/变量名
     */
    private String name = "";

    /**
     * 类型描述信息
     */
    private String desc = "";

    /**
     * signature：泛型中才会将该属性编译进字节码文件，JDK 1.5才加入，除了方法参数和返回值，还包含了泛型信息；
     */
    private String signature = "";

    @Override
    public String toString() {
        try {
            return GsonTools.getJsonOriginStr(this, true);
        } catch (Exception e) {
            return super.toString();
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * 使用owner+name生成md5字符串，用于数据库索引(直接字符串拼接)
     */
    public String getMd5Index() {
        return DigestUtils.md5Hex(owner + name).substring(8, 24);
    }
}
