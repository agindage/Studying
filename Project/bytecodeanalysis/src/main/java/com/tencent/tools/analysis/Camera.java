package com.tencent.tools.analysis;

import com.tencent.tools.common.ByteCodeTools;

public class Camera <T> {
    private T mShutter;
    Phone mPhone = new Phone();
    String haha = "ddd";

    public Camera() throws Exception {
    }

    public T takePicture(T people){
        mShutter = people;
        Phone phone = new Phone();
        int i = phone.mEarPhone;
        phone.takePicture();
        return mShutter;
    }

    public String takePicture2(){return "";}
    public void takePicture(int a){}
    public void takePicture(int a, String b){}
}
