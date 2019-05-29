package com.iwcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yanggs.jhook.JXposed;
import com.yanggs.jhook.MethodCallback;
import com.yanggs.jhook.MethodHookParam;
import com.yanggs.jhook.utils.JXposedHelpers;

import java.lang.reflect.Method;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JXposed.findAndHookConstructor(MyTest.class, new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: MyTest Constructor");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: MyTest Constructor");
            }
        });

        JXposed.findAndHookConstructor(MyTest.class, String.class,int.class,new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: MyTest 有参数 Constructor");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: MyTest 有参数 Constructor");
            }
        });

        JXposed.findAndHookMethod(MyTest.class, "testFun", String.class,int.class,new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: testFun");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: testFun");

            }
        });

        JXposed.findAndHookMethod(MyTest.class, "testFunP", String.class,int.class,new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: testFunP");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: testFunP");

            }
        });

        JXposed.findAndHookMethod(MyTest.class, "testFun2", String.class,int.class,new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod: testFunP");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod: testFunP");

            }
        });

        MyTest myTest = new MyTest("ygs1",1);
        myTest.testFun("ygs2",2);
        myTest.testFunP("ygs3",3);
        try{
            JXposedHelpers.callMethod(new MyTest(),"testFun2","ygs4", 4);
        }catch (Exception e){
            Logger.i(e.getMessage());
            e.printStackTrace();
        }
    }
}
