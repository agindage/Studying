package com.tencent.tools.common.config;

import com.tencent.tools.common.TextUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 全局配置
 *
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-03 16:47
 **/
public class GlobalConfig {
    private static volatile GlobalConfig mInstance;

    private String resultSaveDir;

    private Set<String> blackPackagePrefixSet = new LinkedHashSet<>();

    private Set<String> whiteClassSet = new HashSet<>();

    public static Set<String> sDependClasses = new TreeSet<String>();

    public static GlobalConfig getInstance() {
        if (null == mInstance) {
            synchronized (GlobalConfig.class) {
                if (null == mInstance) {
                    mInstance = new GlobalConfig();
                }
            }
        }
        return mInstance;
    }

    private GlobalConfig() {
    }

    public String getResultSaveDir() {
        return resultSaveDir;
    }

    public void setResultSaveDir(String resultSaveDir) {
        this.resultSaveDir = resultSaveDir;
    }

    public void addBlackPackagePrefix(String blackPackagePrefix) {
        if (TextUtils.isEmptyOrBlank(blackPackagePrefix)) {
            return;
        }
        blackPackagePrefixSet.add(blackPackagePrefix.replaceAll("\\.", "/"));
    }

    public Set<String> getBlackPackagePrefixSet() {
        return blackPackagePrefixSet;
    }

    public void addWhiteClass(String whiteClass) {
        if (TextUtils.isEmptyOrBlank(whiteClass)) {
            return;
        }
        whiteClassSet.add(whiteClass);
    }

    public void addWhiteClass(Set<String> whiteClassSet) {
        if (whiteClassSet == null || whiteClassSet.isEmpty()) {
            return;
        }
        this.whiteClassSet.addAll(whiteClassSet);
    }

    public Set<String> getWhiteClassSet() {
        return whiteClassSet;
    }

    public void addDependClass(String classNmae) {
        if (TextUtils.isEmptyOrBlank(classNmae)) {
            return;
        }
        sDependClasses.add(classNmae);
    }

    public Set<String> getDependClasses(){
        return sDependClasses;
    }
}
