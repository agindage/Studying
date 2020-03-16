package com.tencent.tools.analysis;

import com.tencent.tools.common.ByteCodeTools;

public class StringTest {
    public static void main(String[] args) {
//        String a = "abc";
//        String b = "abc";
//        System.out.println(a == b);
//        String c = "abc";
//        String d = new String(a);
//        System.out.println(a == c);
//        System.out.println(a == d);

        //在try执行return后，finally照常执行
//        try {
//            String a = "";
        //switch支持String，但不支持long
//            switch (a){
//                case "abc":
//                    break;
//            }
//                long b=0;
//                switch (b){
//                    case 1:
//                        break;
//                }
//            return;
//        } catch (Exception e){
//
//        } finally {
//            System.out.println("this is finally");
//        }
//        System.out.println("this is outside");

//        try {
//            System.out.println(ByteCodeTools.convertJniTypeToJavaType("[[Lcom/tencent/qqmini/sdk/minigame/GameRuntime;"));
//            System.out.println(ByteCodeTools.convertJniTypeToJavaType("Landroid/view/ViewGroup;"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println(ByteCodeTools.convertAccessFlagToString(0x3));
    }
}
