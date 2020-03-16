package com.tencent.tools.analysis;

public class Phone {
    Camera mCamera;
    public int mEarPhone;
    public Phone(){
        try {
            mCamera = new Camera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void takePicture(){
        mCamera.takePicture("girl");
    }
}
