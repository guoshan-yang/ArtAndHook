package com.iwcode.jhook;


import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Created by YangGuoShan on 2019/5/27.
 */
public class HookUtil {

    public static void replaceMethod(BackMethod old){
        replaceNativeArt(old.getOldMethod(), old.getNewMethod(),old.getInvoker());
    }

    public static boolean isArt(){
        final String vmVersion = System.getProperty("java.vm.version");
        return vmVersion != null && vmVersion.startsWith("2");
    }

    private static native void replaceNativeArt(Member oldMethod, Member newMethod, Method invoker);
}
