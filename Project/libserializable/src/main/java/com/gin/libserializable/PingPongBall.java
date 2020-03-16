package com.gin.libserializable;

import java.io.Serializable;
import java.util.Arrays;

public class PingPongBall extends Ball implements Serializable {
//    private final int serialVersionUID = 1;
    Integer mWeight = 2;
    String mColor = "";
    int mRadius;
    public static int sElasticity;//静态变量不会被序列化
    transient int mHardness;//被transient修饰后不能序列化
    transient int[] mSizes;//被transient修饰后如果想要序列化，则自己实现writeObject和readObject
    public PingPongBall(){
        mColor = "white";
        mRadius = 10;
        sElasticity = 200;
        mHardness = 300;
    }

    public PingPongBall(String color, int radius, int elasticity, int hardness){
        mColor =color;
        mRadius = radius;
        sElasticity = elasticity;
        mHardness = hardness;
        mSizes = new int[10];
        for (int i=0; i<5; i++){
            mSizes[i] = i;
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        //执行 JVM 默认的序列化操作
        s.defaultWriteObject();

        //手动序列化 arr  前面5个元素
        for (int i = 0; i < 5; i++) {
            s.writeInt(mSizes[i]);
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {

        s.defaultReadObject();
        mSizes = new int[10];

        // Read in all elements in the proper order.
        for (int i = 0; i < 5; i++) {
            mSizes[i] = s.readInt();
        }
    }

    @Override
    public String toString(){
        return "color = " + mColor +
                ", mRadius = " + mRadius +
                ", sElasticity = " + sElasticity +
                ", mHardness = " + mHardness +
                ", mSizes = " + Arrays.toString(mSizes);
    }
}
