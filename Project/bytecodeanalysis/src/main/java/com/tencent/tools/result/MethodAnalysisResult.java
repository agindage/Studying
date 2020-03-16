package com.tencent.tools.result;

import com.tencent.tools.common.GsonTools;
import com.tencent.tools.common.TextUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 函数调用解析结果基类
 *
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-06 19:09
 **/
public class MethodAnalysisResult implements Serializable {
    /**
     * 调用类名
     */
    private String callerOwner = "";
    /**
     * 调用方法/变量名
     */
    private String callerName = "";
    /**
     *
     * descriptor：主要是对方法参数和返回值进行描述；
     * 调用方法/变量描述信息，包含返回值和参数
     */
    private String callerDesc = "";

    /**
     * signature：泛型中才会将该属性编译进字节码文件，JDK 1.5才加入，除了方法参数和返回值，还包含了泛型信息；
     */
    private String signature = "";

    /**
     * 调用列表
     */
    private List<MethodCalleeDetail> calleeList = new ArrayList<>();

    public String getCallerOwner() {
        return callerOwner;
    }

    public void setCallerOwner(String callerOwner) {
        this.callerOwner = callerOwner;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerDesc() {
        return callerDesc;
    }

    public void setCallerDesc(String callerDesc) {
        this.callerDesc = callerDesc;
    }

    public List<MethodCalleeDetail> getCalleeList() {
        return calleeList;
    }

    public void setCalleeList(List<MethodCalleeDetail> calleeList) {
        this.calleeList = calleeList;
    }

    public void addCallee(MethodCalleeDetail methodCalleeDetail) {
        if (null == calleeList) {
            calleeList = new ArrayList<>();
        }

        calleeList.add(methodCalleeDetail);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * 使用callerOwner + callerName + callerDesc生成md5字符串，用于数据库索引(直接字符串拼接)
     */
    public String getCallerMd5Index() {
        return DigestUtils.md5Hex(callerOwner + callerName + callerDesc).substring(8, 24);
    }

    public static class MethodCalleeDetail {
        /**
         * 调用类型
         */
        private CallType callType = CallType.UNKNOWN_TYPE;
        /**
         * 行号
         */
        private int line = 0;
        /**
         * 被调类名
         */
        private String calleeOwner = "";
        /**
         * 被调方法/变量名
         */
        private String calleeName = "";
        /**
         * 被调方法/变量描述信息，包含返回值和参数
         */
        private String calleeDesc = "";

        public CallType getCallType() {
            return callType;
        }

        public void setCallType(CallType callType) {
            this.callType = callType;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public String getCalleeOwner() {
            return calleeOwner;
        }

        public void setCalleeOwner(String calleeOwner) {
            this.calleeOwner = calleeOwner;
        }

        public String getCalleeName() {
            return calleeName;
        }

        public void setCalleeName(String calleeName) {
            this.calleeName = calleeName;
        }

        public String getCalleeDesc() {
            return calleeDesc;
        }

        public void setCalleeDesc(String calleeDesc) {
            this.calleeDesc = calleeDesc;
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
         * 使用calleeOwner + calleeName + calleeDesc生成md5字符串，用于数据库索引(直接字符串拼接)
         */
        public String getCalleeMd5Index() {
            return DigestUtils.md5Hex(calleeOwner + calleeName + calleeDesc).substring(8, 24);
        }
    }

    @Override
    public String toString() {
        try {
            return GsonTools.getJsonOriginStr(this, true);
        } catch (Exception e) {
            return super.toString();
        }
    }
}
