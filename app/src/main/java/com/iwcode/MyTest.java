package com.iwcode;

/**
 * Created by YangGuoShan on 2019/5/28.
 */
public class MyTest {

    public  MyTest() {
        Logger.i("MyTest:");
    }

    public MyTest(String s,int i) {
        Logger.i("MyTest:"+s+","+i);
    }

    public void testFun(String s,int i){
        Logger.i("testFun:"+s+","+i);
    }

    public static void testFunP(String s,int i){
        Logger.i("testFunP:"+s+","+i);
    }

    public void testFun2(String s,int i){
        Logger.i("testFun:"+s+","+i);
    }

}
