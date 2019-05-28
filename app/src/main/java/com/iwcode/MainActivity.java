package com.iwcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iwcode.jhook.JXposed;
import com.iwcode.jhook.MethodCallback;
import com.iwcode.jhook.MethodHookParam;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        JXposed.findAndHookMethod(MainActivity.class, "testFun", String.class,int.class,new MethodCallback() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Logger.i("beforeHookedMethod:"+param.method.getName());
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Logger.i("afterHookedMethod:"+param.method.getName());

            }
        });

        testFun("ygs",123);
    }

    private void testFun(String s,int i){
        Logger.i("testPrivate:"+s+","+i);
    }
}
