package com.tencent.tools.result;

public class JarStatistic {
    private int mClassNum = 0;
    private int mFieldNum = 0;
    private int mMethodNum = 0;
    public JarStatistic(){}
    public void increateClass(){
        mClassNum ++;
    }

    public void increateClass(int n){
        mClassNum += n;
    }

    public void decreateClass(){
        mClassNum --;
    }

    public void increateField(){
        mFieldNum ++;
    }

    public void increateField(int n){
        mFieldNum += n;
    }

    public void decreateField(){
        mFieldNum --;
    }

    public void increateMethod(){
        mMethodNum ++;
    }

    public void increateMethod(int n){
        mMethodNum +=n;
    }

    public void decreateMethod() {
        mMethodNum--;
    }

    public int getClassNum(){
        return mClassNum;
    }
    public int getFieldNum(){
        return mFieldNum;
    }
    public int getMethodNum(){
        return mMethodNum;
    }

    public void reset(){
        mClassNum = mFieldNum = mMethodNum = 0;
    }
}
