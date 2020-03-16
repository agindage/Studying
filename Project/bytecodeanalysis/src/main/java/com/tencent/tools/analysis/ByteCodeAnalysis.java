package com.tencent.tools.analysis;

import static com.tencent.tools.result.CallType.METHOD_CALL;
import static org.objectweb.asm.Opcodes.ASM7;

import com.tencent.tools.common.ByteCodeTools;
import com.tencent.tools.common.FileUtils;
import com.tencent.tools.common.GsonTools;
import com.tencent.tools.common.TextUtils;
import com.tencent.tools.common.config.GlobalConfig;
import com.tencent.tools.result.CallType;
import com.tencent.tools.result.ClassAnalysisResult;
import com.tencent.tools.result.FieldAnalysisResult;
import com.tencent.tools.result.JarStatistic;
import com.tencent.tools.result.MethodAnalysisResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * @author caesarzhli
 * @program ByteCodeAnalysisTool
 * @date 2020-01-02 15:56
 **/
public class ByteCodeAnalysis {
    static String sOutputPath = new File("").getAbsolutePath() + File.separator + "analyze_result";
    static String sTempPath =sOutputPath + File.separator + "tmp";
    static String sResultJsonPath = sOutputPath + File.separator + "json";
    static String sResultListPath = sOutputPath + File.separator + "classes.txt";
    static String sDestPath = "";
    static String sDestName = "";
    static ArrayList<String> sSrcPaths = new ArrayList();
    static ArrayList<String> sSrcNames = new ArrayList();
    static HashMap<String, Set<String>> sClass2MethodFiledMap = new HashMap<>();
    static JarStatistic sSrcStatistic = new JarStatistic();
    static JarStatistic sDestStatistic = new JarStatistic();
    static JarStatistic sDependStatistic = new JarStatistic();
    static long sStartTime;
    public static void main(String[] args){
        if (args == null || args.length == 0){
            args = "-d D:\\Workspace\\MiniApp\\lib_minisdk\\build\\outputs\\aar\\lib_minisdk-debug.aar -s D:\\Workspace\\MiniApp\\lib_minigame\\build\\outputs\\aar\\lib_minigame-debug.aar -s D:\\Workspace\\MiniApp\\lib_miniapp\\build\\outputs\\aar\\lib_miniapp-debug.aar".split(" ");
        }
        sStartTime = System.currentTimeMillis();
        parseArgs(args);
        try {
            ArrayList<String> tmpSrc = (ArrayList<String>)sSrcPaths.clone();
            String tmpDepnd = sDestPath;
            for (String s : sSrcPaths){
                sSrcNames.add(new File(s).getName());
            }
            sDestName = new File(sDestPath).getName();
            sSrcPaths = getValidPath(sSrcPaths);
            sDestPath = getValidPath(sDestPath);
            if (sSrcPaths.size() == 0 || TextUtils.isEmpty(sDestPath)){
                System.out.println("file path not valid, please check again, src =" + tmpSrc.toArray() + ", dest = " + tmpDepnd);
                return;
            }
            //黑名单
            GlobalConfig.getInstance().addBlackPackagePrefix("android.");
            //白名单
            Set<String> destPackage = obtainPackageList(sDestPath);
            GlobalConfig.getInstance().addWhiteClass(destPackage);
            System.out.println("---------------------------------------------------------------------------------------");
            //解析
            for (int i=0; i<sSrcPaths.size(); i++){
                ByteCodeAnalysis.analysisByteCodeAsm(sSrcPaths.get(i), sResultJsonPath, "gin", false);
                System.out.println(String.format("%-30s | Class num:%-5s | Field num:%-5s | Method num:%-5s", "name:" + sSrcNames.get(i), sSrcStatistic.getClassNum(), sSrcStatistic.getFieldNum(),sSrcStatistic.getMethodNum()));
                sSrcStatistic.reset();
            }
            //输出
            saveDependClassList();
            System.out.println(String.format("%-30s | Class num:%-5s | Field num:%-5s | Method num:%-5s", "name:" + sDestName, sDestStatistic.getClassNum(), sDestStatistic.getFieldNum(),sDestStatistic.getMethodNum()));
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println(String.format("%-30s | Class num:%-5s | Field num:%-5s | Method num:%-5s", sSrcNames.toString() + "->" + sDestName, sDependStatistic.getClassNum(), sDependStatistic.getFieldNum(),sDependStatistic.getMethodNum()));
            System.out.println("---------------------------------------------------------------------------------------");

            System.out.println("cost time:" + (System.currentTimeMillis() - sStartTime));
            FileUtils.deleteDirectory(sTempPath);
        } catch (ByteCodeAnalysisException e) {
            e.printStackTrace();
        }
    }

    private static void parseArgs(String[] args){
        for (int i=0; i<args.length; i++){
            if ("-s".equals(args[i])){
                sSrcPaths.add(args[i+1]);
            } else if ("-d".equals(args[i])){
                sDestPath = args[i+1];
            }
        }
    }

    private static ArrayList<String> getValidPath(ArrayList<String> srcPaths){
        ArrayList<String> resultPaths = new ArrayList<>();
        for (String srcPath : srcPaths){
            File sFile = new File(srcPath);
            if (!sFile.exists()){
                return resultPaths;
            }
            if (!sFile.getName().endsWith(".jar") && !sFile.getName().endsWith(".aar")){
                System.out.println("文件错误，仅支持aar或jar");
                return resultPaths;
            }
            String resultPath = srcPath;
            if (sFile.getName().endsWith(".aar")){
                resultPath = sTempPath + File.separator + sFile.getName().substring(0, sFile.getName().length() - 4);
                File resultFile = new File(resultPath);
                if (!resultFile.exists()){
                    boolean mkResult = resultFile.mkdirs();
                    if (!mkResult){
                        return resultPaths;
                    }
                } else {
                    FileUtils.deleteDirectory(resultPath);
                }
                int code = FileUtils.unZipFolder(srcPath, resultPath);
                if (code != 0){
                    return resultPaths;
                }
                resultPath = resultPath + File.separator + "classes.jar";
            }
            resultPaths.add(resultPath);
        }

        return resultPaths;
    }

    private static String getValidPath(String srcPath){
        File sFile = new File(srcPath);
        if (!sFile.exists()){
            return "";
        }
        if (!sFile.getName().endsWith(".jar") && !sFile.getName().endsWith(".aar")){
            System.out.println("文件错误，仅支持aar或jar");
            return "";
        }
        String resultPath = srcPath;
        if (sFile.getName().endsWith(".aar")){
            resultPath = sTempPath + File.separator + sFile.getName().substring(0, sFile.getName().length() - 4);
            File resultFile = new File(resultPath);
            if (!resultFile.exists()){
                boolean mkResult = resultFile.mkdirs();
                if (!mkResult){
                    return "";
                }
            } else {
                FileUtils.deleteDirectory(resultPath);
            }
            int code = FileUtils.unZipFolder(srcPath, resultPath);
            if (code != 0){
                return "";
            }
            resultPath = resultPath + File.separator + "classes.jar";
        }
        return resultPath;
    }

    private static void saveDependClassList(){
        File classNamsFile = new File(sResultListPath);
        if (!classNamsFile.exists()){
            try {
                boolean success = classNamsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(classNamsFile, false);
            //遍历依赖jar，为了获取access flag
            try (JarFile jarFile = new JarFile(sDestPath)){
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String name = entry.getName();
                        name = name.substring(0, name.length() - ".class".length());
                        ClassReader reader = null;
                        reader = new ClassReader(jarFile.getInputStream(entry));
                        ClassNode clsNode = new ClassNode(ASM7);
                        reader.accept(clsNode, ASM7);
                        if (sClass2MethodFiledMap.containsKey(name)){
                            //依赖中有这个类，则需要遍历一下这个类中的所有成员和方法
                            Set<String> methodAndFields = sClass2MethodFiledMap.get(name);
                            String restore = restoreDependDetail(methodAndFields, clsNode);
                            writer.write(restore);
                        }
                        sDestStatistic.increateClass();
                        sDestStatistic.increateField(clsNode.fields.size());
                        sDestStatistic.increateMethod(clsNode.methods.size());
                    }
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static String restoreDependDetail(Set<String> methodAndFields, ClassNode clsNode) throws Exception {
        if (methodAndFields == null || methodAndFields.size() == 0){
            return "{}";
        }
        StringBuilder result = new StringBuilder();
        try {
            //类名
            result.append(ByteCodeTools.convertAccessFlagToString(clsNode.access) + ByteCodeTools.convertByteCodeOwnerToOrigin(clsNode.name));
            result.append("{\n");

            for (FieldNode fieldNode : clsNode.fields) {
                String fieldName = fieldNode.name;
                String fieldDesc = fieldNode.desc;
                if (!methodAndFields.contains(fieldName + "^" + fieldDesc)){
                    continue;
                } else {
                    String accFlag = ByteCodeTools.convertAccessFlagToString(fieldNode.access);
                    String fieldType = ByteCodeTools.convertJniTypeToJavaType(fieldNode.desc);
                    result.append("    ");
                    result.append(accFlag);
                    result.append(fieldType);
                    result.append(" ");
                    result.append(fieldName);
                    result.append(";");
                    result.append("\n");
                    sDependStatistic.increateField();
                }
            }

            for (MethodNode methodNode : clsNode.methods) {
                String methodName = methodNode.name;
                String methodDesc = methodNode.desc;
                if (!methodAndFields.contains(methodName + "|" + methodDesc)){
                    continue;
                } else {
                    String accFlag = ByteCodeTools.convertAccessFlagToString(methodNode.access);
                    String methodAndParams = ByteCodeTools.convertMethodDescriptor(methodName, methodDesc);
                    result.append("    ");
                    result.append(accFlag);
                    result.append(methodAndParams);
                    result.append(";");
                    result.append("\n");
                    sDependStatistic.increateMethod();
                }
            }
            result.append("}\n");
            sDependStatistic.increateClass();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static Set<String> obtainPackageList(String path){
        if (TextUtils.isEmptyOrBlank(path)){
            return null;
        }
        File file = new File(path);
        if (!file.exists()){
            return null;
        }
        Set<String> result = new HashSet<>();
        if (file.isFile() && path.endsWith(".jar")){
            searchAllClassByJar(path, result);
        } else if (file.isDirectory()){
            searchAllClassByDir(file, "", result);
        }
        return result;
    }

    private static void searchAllClassByJar(String byteCodeFilePath, Set<String> result){
        try (JarFile jarFile = new JarFile(byteCodeFilePath)){
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String name = entry.getName();
                    result.add(name.substring(0, name.length() - ".class".length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void searchAllClassByDir(File parentFile, String prefix, Set<String> result){
        File[] subFiles = parentFile.listFiles();
        for (File f : subFiles){
            if (f.isDirectory()){
                if (TextUtils.isEmptyOrBlank(prefix)){
                    searchAllClassByDir(f, f.getName(), result);
                } else {
                    searchAllClassByDir(f, prefix + "/" + f.getName(), result);
                }
            } else {
                String name = f.getName();
                result.add(prefix+"/" + name.substring(0, name.length() - ".class".length()));
            }
        }
    }

    public static void analysisByteCodeAsm(String byteCodeFilePath, String analysisResultSaveBaseDir,
                                           String moduleName, boolean jsonPretty) throws ByteCodeAnalysisException {
        if (null == byteCodeFilePath) {
            System.out.println("null path");
            return;
        }

        File sourceFile = new File(byteCodeFilePath);
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            throw new ByteCodeAnalysisException(String.format("%s not found or is not a file!", byteCodeFilePath));
        }

        try (JarFile jarFile = new JarFile(byteCodeFilePath)){
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        analysisByteCodeAsm(inputStream, analysisResultSaveBaseDir, moduleName, jsonPretty);
                        sSrcStatistic.increateClass();
                    } catch (Exception e) {
                        System.err.println(String.format("%s analysis failed!", entry.getName()));
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            //非jar文件
            try (FileInputStream fileInputStream = new FileInputStream(sourceFile)) {
                analysisByteCodeAsm(fileInputStream, analysisResultSaveBaseDir, moduleName, jsonPretty);
            } catch (Exception e1) {
                throw new ByteCodeAnalysisException("error", e1);
            }
        }
    }

    private static void analysisByteCodeAsm(InputStream inputStream, String analysisResultSaveBaseDir, String moduleName,
                                           boolean jsonPretty) throws Exception {

        ClassReader reader = new ClassReader(inputStream);
        ClassNode clsNode = new ClassNode(ASM7);
        reader.accept(clsNode, ASM7);
        ClassAnalysisResult classAnalysisResult = new ClassAnalysisResult();
        classAnalysisResult.setInterfaces(clsNode.interfaces);
        classAnalysisResult.setName(clsNode.name);
        classAnalysisResult.setSuperName(clsNode.superName);
        classAnalysisResult.setSourceFile(clsNode.sourceFile);
        classAnalysisResult.setSignature(clsNode.signature);
        classAnalysisResult.setModuleName(moduleName);

        boolean matchMethod = false;
        boolean matchFiled = false;
        for (MethodNode methodNode:clsNode.methods) {
            matchMethod = matchMethod | analysisMethodNode(clsNode, methodNode, classAnalysisResult);
            sSrcStatistic.increateMethod();
        }

        for (FieldNode fieldNode:clsNode.fields) {
            matchFiled = matchFiled | analysisFieldNode(clsNode, fieldNode, classAnalysisResult);
            sSrcStatistic.increateField();
        }

        boolean isBlack = false;
        for (String blackPrefix:GlobalConfig.getInstance().getBlackPackagePrefixSet()) {
            if (clsNode.superName.startsWith(blackPrefix)) {
                isBlack = true;
                break;
            }
        }

        if (!isBlack && GlobalConfig.getInstance().getWhiteClassSet().contains(clsNode.superName)){
            if (!sClass2MethodFiledMap.containsKey(clsNode.superName)){
                sClass2MethodFiledMap.put(clsNode.superName, new HashSet<String>());
            }
        }
        if (!matchMethod && !matchFiled){
            return;
        }

        String resultSaveFilePath = analysisResultSaveBaseDir + "/" + clsNode.name + ".json";
        GsonTools.saveJsonToFile(resultSaveFilePath, classAnalysisResult, jsonPretty, null);
        classifyMethodAndField(classAnalysisResult);
    }

    /**
     * 把依赖的类放到一个集合
     * key - 类名
     * value - 方法名 或 成员 名称
     * */
    private static void classifyMethodAndField(ClassAnalysisResult classAnalysisResult){
        if (classAnalysisResult == null || classAnalysisResult.getMethodAnalysisResults() == null){
            return;
        }
        List<MethodAnalysisResult> methodAnalysisResults = classAnalysisResult.getMethodAnalysisResults();
        for (MethodAnalysisResult method: methodAnalysisResults){
            List<MethodAnalysisResult.MethodCalleeDetail> methodCalleeDetails = method.getCalleeList();
            for (MethodAnalysisResult.MethodCalleeDetail detail : methodCalleeDetails){

                switch (detail.getCallType()){
                    case METHOD_CALL:
                    case FIELD_ACCESS:
                        Set<String> methodAndFields = sClass2MethodFiledMap.get(detail.getCalleeOwner());
                        if (methodAndFields == null){
                            methodAndFields = new HashSet<String>();
                            sClass2MethodFiledMap.put(detail.getCalleeOwner(), methodAndFields);
                        }
                        if (detail.getCallType() == METHOD_CALL){
                            //如果是方法调用，则value是 方法名+desc
                            methodAndFields.add(detail.getCalleeName() + "|" + detail.getCalleeDesc());
                        } else {
                            //如果是成员变量，则value是
                            methodAndFields.add(detail.getCalleeName() + "^"  + detail.getCalleeDesc());
                        }
                        break;
                    case NEW_EXPR:
                    case ANEW_ARRAY:
                    case CHECK_CAST:
                    case INSTANCE_OF:
                        //这几种情况，只有类名
                        if (!sClass2MethodFiledMap.containsKey(detail.getCalleeDesc())){
                            sClass2MethodFiledMap.put(detail.getCalleeDesc(), new HashSet<String>());
                        }
                        break;
                }
            }
        }
    }

    private static boolean analysisFieldNode(ClassNode classNode, FieldNode fieldNode, ClassAnalysisResult classAnalysisResult) {
        boolean isBlack = false;
        for (String blackPrefix:GlobalConfig.getInstance().getBlackPackagePrefixSet()) {
            if (fieldNode.desc.startsWith(blackPrefix)) {
                isBlack = true;
                break;
            }
        }

        if (isBlack) {
            return false;
        }
        if (!GlobalConfig.getInstance().getWhiteClassSet().contains(fieldNode.desc)) {
            return false;
        }

        FieldAnalysisResult fieldAnalysisResult = new FieldAnalysisResult();
        fieldAnalysisResult.setOwner(classNode.name);
        fieldAnalysisResult.setDesc(fieldNode.desc);
        fieldAnalysisResult.setName(fieldNode.name);
        fieldAnalysisResult.setSignature(fieldNode.signature);

        classAnalysisResult.addFieldAnalysisResult(fieldAnalysisResult);
        return true;
    }

    private static boolean analysisMethodNode(ClassNode classNode, MethodNode methodNode, ClassAnalysisResult classAnalysisResult) {
        MethodAnalysisResult methodAnalysisResult = new MethodAnalysisResult();
        methodAnalysisResult.setCallerOwner(classNode.name);
        methodAnalysisResult.setCallerName(methodNode.name);
        methodAnalysisResult.setCallerDesc(methodNode.desc);
        methodAnalysisResult.setSignature(methodNode.signature);

        MethodAnalysisResult.MethodCalleeDetail methodCalleeDetail;
        Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        int curLine = -1;
        boolean isBlack = false;
        boolean isWhite = false;
        boolean match = false;
        String dependClassName;
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            isWhite = isBlack = false;
            if (node instanceof LineNumberNode) {
                curLine = ((LineNumberNode) node).line;
            } else{
                if (node instanceof MethodInsnNode) {
                    for (String blackPrefix:GlobalConfig.getInstance().getBlackPackagePrefixSet()) {
                        if (((MethodInsnNode) node).owner.startsWith(blackPrefix)) {
                            isBlack = true;
                            break;
                        }
                    }

                    if (isBlack) {
                        continue;
                    }
                    String owner = ((MethodInsnNode) node).owner;
                    if (!GlobalConfig.getInstance().getWhiteClassSet().contains(owner)){
                        continue;
                    }

                    methodCalleeDetail = new MethodAnalysisResult.MethodCalleeDetail();
                    methodCalleeDetail.setLine(curLine);
                    methodCalleeDetail.setCallType(METHOD_CALL);
                    methodCalleeDetail.setCalleeOwner(((MethodInsnNode) node).owner);
                    methodCalleeDetail.setCalleeName(((MethodInsnNode) node).name);
                    methodCalleeDetail.setCalleeDesc(((MethodInsnNode) node).desc);
                    methodAnalysisResult.addCallee(methodCalleeDetail);
                    match = true;
                } else if (node instanceof FieldInsnNode) {
                    for (String blackPrefix:GlobalConfig.getInstance().getBlackPackagePrefixSet()) {
                        if (((FieldInsnNode) node).owner.startsWith(blackPrefix)) {
                            isBlack = true;
                            break;
                        }
                    }

                    if (isBlack) {
                        continue;
                    }
                    if (!GlobalConfig.getInstance().getWhiteClassSet().contains(((FieldInsnNode) node).owner)) {
                        continue;
                    }

                    methodCalleeDetail = new MethodAnalysisResult.MethodCalleeDetail();
                    methodCalleeDetail.setLine(curLine);
                    methodCalleeDetail.setCallType(CallType.FIELD_ACCESS);
                    methodCalleeDetail.setCalleeOwner(((FieldInsnNode) node).owner);
                    methodCalleeDetail.setCalleeName(((FieldInsnNode) node).name);
                    methodCalleeDetail.setCalleeDesc(((FieldInsnNode) node).desc);
                    methodAnalysisResult.addCallee(methodCalleeDetail);
                    match = true;
                } else if (node instanceof TypeInsnNode) {
                    methodCalleeDetail = new MethodAnalysisResult.MethodCalleeDetail();
                    methodCalleeDetail.setLine(curLine);
                    for (String blackPrefix:GlobalConfig.getInstance().getBlackPackagePrefixSet()) {
                        if (((TypeInsnNode) node).desc.startsWith(blackPrefix)) {
                            isBlack = true;
                            break;
                        }
                    }

                    if (isBlack) {
                        continue;
                    }
                    if (!GlobalConfig.getInstance().getWhiteClassSet().contains(((TypeInsnNode) node).desc)) {
                        continue;
                    }

                    if (node.getOpcode() == Opcodes.INSTANCEOF) {
                        methodCalleeDetail.setCallType(CallType.INSTANCE_OF);
                        methodCalleeDetail.setCalleeDesc(((TypeInsnNode) node).desc);
                        methodAnalysisResult.addCallee(methodCalleeDetail);
                    } else if (node.getOpcode() == Opcodes.CHECKCAST) {
                        methodCalleeDetail.setCallType(CallType.CHECK_CAST);
                        methodCalleeDetail.setCalleeDesc(((TypeInsnNode) node).desc);
                        methodAnalysisResult.addCallee(methodCalleeDetail);
                    } else if (node.getOpcode() == Opcodes.ANEWARRAY) {
                        methodCalleeDetail.setCallType(CallType.ANEW_ARRAY);
                        methodCalleeDetail.setCalleeDesc(((TypeInsnNode) node).desc);
                        methodAnalysisResult.addCallee(methodCalleeDetail);
                    } else if (node.getOpcode() == Opcodes.NEW) {
                        methodCalleeDetail.setCallType(CallType.NEW_EXPR);
                        methodCalleeDetail.setCalleeDesc(((TypeInsnNode) node).desc);
                        methodAnalysisResult.addCallee(methodCalleeDetail);
                    }
                    match = true;
                }
            }
        }
        if (match){
            classAnalysisResult.addMethodAnalysisResult(methodAnalysisResult);
        }
        return match;
    }
}
