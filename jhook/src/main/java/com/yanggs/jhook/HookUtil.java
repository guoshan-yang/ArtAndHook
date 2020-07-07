package com.yanggs.jhook;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Created by YangGuoShan on 2018/5/27.
 */
public class HookUtil {

    int test;

    static {
        System.loadLibrary("native-lib");
        try{
            if (HookUtil.isArt()){
                computeSupperCls(Object.class.getDeclaredFields()[0], HookUtil.class.getDeclaredField("test"));
            }
        }catch (Exception e){e.printStackTrace();}
    }

    static void replaceMethod(BackMethod old){
        replaceNativeArt(old.getOldMethod(), old.getNewMethod(),old.getInvoker());
    }

    static boolean isArt(){
        final String vmVersion = System.getProperty("java.vm.version");
        return vmVersion != null && vmVersion.startsWith("2");
    }
    public static boolean setMadeClassSuper(Class cls){
        try{
            Field flag=cls.getField("flag");
            setSupperCls(flag);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private static native void replaceNativeArt(Member oldMethod, Member newMethod, Method invoker);
    private static native void computeSupperCls(Member fld, Member test);
    private static native void setSupperCls(Member flag);
}
