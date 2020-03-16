package com.tencent.tools.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import sun.rmi.runtime.Log;

public class FileUtils {
    /**
     * 解压缩一个文件
     *
     * @param zipFilePath 压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static int unZipFolder(String zipFilePath, String folderPath)  {
        if(TextUtils.isEmpty(folderPath)){
            return 9;
        }
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        File zipFile = new File(zipFilePath);
        ZipFile zf;
        try{
            zf = new ZipFile(zipFile);
        }catch(ZipException e){
            return 1;
        }catch( IOException e){
            return 2;
        } catch(Exception e){
            return 3;
        }
        int code=0;
        try{


            byte[] b=new byte[8192];
            StringBuilder stringBuilder = new StringBuilder(512);
            for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
                ZipEntry entry = ((ZipEntry)entries.nextElement());
                String szName = entry.getName();
                if(szName.contains("..")){
                    continue;
                }
                stringBuilder.delete(0,stringBuilder.length());
                stringBuilder.append(folderPath).append(File.separator).append(szName);
                if (entry.isDirectory()) {

                    File entryFile = new File(stringBuilder.toString());
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }

                }else{
                    InputStream in = null;
                    CheckedInputStream csumi = null;
                    OutputStream out = null;
                    try {
                        in = zf.getInputStream(entry);
                        csumi = new CheckedInputStream(in, new CRC32());
                        String str = new String(stringBuilder.toString().getBytes("8859_1"), "GB2312");
                        File desFile = new File(str);
                        if (!desFile.exists()) {
                            File fileParentDir = desFile.getParentFile();
                            if (!fileParentDir.exists()) {
                                fileParentDir.mkdirs();
                            }
                        }
                        out = RecycleableBufferedOutputStream.obtain(new BufferedOutputStream(new FileOutputStream(desFile)));

                        long size = entry.getSize();
                        int l;
                        while (size > 0) {
                            l = csumi.read(b, 0, 8192);
                            out.write(b, 0, l);
                            size -= l;
                        }
                        if (entry.getCrc() != csumi.getChecksum().getValue()) {
                            code = 4;

                            break;
                        }
                    } finally {
                        if (csumi != null) {
                            try {
                                csumi.close();
                            } catch (IOException ignored) {}
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ignored) {}
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ignored) {}
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //isFail = true;
            code=5;
        } catch (IOException e) {
            e.printStackTrace();
            //isFail = true;
            code=6;
        } catch(Exception e){
            e.printStackTrace();
            //isFail = true;
            code=7;
        }

        try {
            zf.close();
        } catch (IOException e) {
            e.printStackTrace();
            code=8;
        }
        return code;
    }

    public static void deleteDirectory(String dirStr){
        if(dirStr == null || dirStr.trim().length() == 0){
            return;
        }
        File rootDir = new File(dirStr);

        File[] childDirList = rootDir.listFiles();
        if(childDirList != null && childDirList.length>0){
            for(int i=0;i<childDirList.length;i++){
                if(childDirList[i].isDirectory()){
                    deleteDirectory(childDirList[i].getAbsolutePath());
                }else{
                    childDirList[i].delete();
                }
            }
        }
        rootDir.delete();
    }
}
