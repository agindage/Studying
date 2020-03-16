package com.gin.libserializable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Main {
    private static final String FILE_PATH = "./ball.bin";

    public static void main(String[] args) throws Exception {
        serializeBall();
        deserializeBall();
    }
    private static void serializeBall() throws Exception {
        PingPongBall ball = new PingPongBall("black", 5, 201, 301);
        System.out.println("序列化前："+ ball.toString());
        System.out.println("=================开始序列化================");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH));
        oos.writeObject(ball);
        oos.flush();
        oos.close();
    }

    private static void deserializeBall() throws Exception {
        System.out.println("=================开始反序列化================");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH));
        PingPongBall ball = (PingPongBall) ois.readObject();
        ois.close();
        System.out.println("反序列化 : " + ball);
    }
}
