package com.iwcode;

/**
 * Created by YangGuoShan on 2018/5/28.
 */
public class MyTest {

    private MyTest(int i) {
        Logger.i("MyTest: i = "+i);
    }

    public  MyTest() {
        Logger.i("MyTest:");
    }

    public MyTest(String s,int i) {
        Logger.i("MyTest:"+s+","+i);
    }

    public void testFun(String s,int i){
        Logger.i("testFun:"+s+","+i);
    }

    public final void testFunFinal(String s,int i){
        Logger.i("testFunFinal:"+s+","+i);
    }

    public void testFun2(String s,int i){
        Logger.i("testFun2:"+s+","+i);
    }

    public static void testFunS(String s,int i){
        Logger.i("testFunS:"+s+","+i);
    }
}
