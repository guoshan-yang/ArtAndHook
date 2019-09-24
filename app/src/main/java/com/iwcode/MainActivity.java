package com.iwcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

//import com.taobao.android.dexposed.DexposedBridge;
//import com.taobao.android.dexposed.XC_MethodHook;
import com.yanggs.jhook.JXposed;
import com.yanggs.jhook.MethodCallback;
import com.yanggs.jhook.MethodHookParam;
import com.yanggs.jhook.utils.JXposedHelpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;



public class MainActivity extends AppCompatActivity {

//    static {
//        System.loadLibrary("native-lib");
//    }

    private String abc(){
        Logger.i("abc-------");
        return "abc";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        JXposed.findAndHookConstructor(MyTest.class, new MethodCallback() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Logger.i("beforeHookedMethod: MyTest Constructor");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Logger.i("afterHookedMethod: MyTest Constructor");
//            }
//        });
//
//        JXposed.findAndHookConstructor(MyTest.class, String.class,int.class,new MethodCallback() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Logger.i("beforeHookedMethod: MyTest 有参数 Constructor");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Logger.i("afterHookedMethod: MyTest 有参数 Constructor");
//            }
//        });
//
//        JXposed.findAndHookMethod(MyTest.class, "testFun", String.class,int.class,new MethodCallback() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Logger.i("beforeHookedMethod: testFun");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Logger.i("afterHookedMethod: testFun");
//
//            }
//        });
//
//        JXposed.findAndHookMethod(MyTest.class, "testFunFinal", String.class,int.class,new MethodCallback() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Logger.i("beforeHookedMethod: testFunFinal");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Logger.i("afterHookedMethod: testFunFinal");
//
//            }
//        });
//
        JXposed.findAndHookMethod(MainActivity.class, "abc",new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: abc");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: abc");
                param.setResult("123");

            }
        });

        String s = abc();
        Logger.i("s = "+s);
//
//        MyTest myTest = new MyTest("ygs1",1);
//        myTest.testFun("ygs2",2);
//        myTest.testFunFinal("ygs3",3);
//        try{
//            JXposedHelpers.callMethod(new MyTest(),"testFun2","ygs4", 4);
//        }catch (Exception e){
//            Logger.i(e.getMessage());
//            e.printStackTrace();
//        }

        // 调用隐藏api
//        boolean b = ReflectionUtil.exemptAll();
//
//        Logger.i("b = "+b);
//
//        DexposedBridge.findAndHookMethod(MyTest.class, "testFunS", String.class, int.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Logger.i("testFunS beforeHookedMethod");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Logger.i("testFunS afterHookedMethod");
//            }
//        });
//
//        MyTest.testFunS("ygs",10);
    }
}
