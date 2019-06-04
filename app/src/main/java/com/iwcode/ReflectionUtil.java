package com.iwcode;

import java.lang.reflect.Method;

/**
 * Created by YangGuoShan on 2019/6/4.
 */
public class ReflectionUtil {

    private static Object sVmRuntime;
    private static Method setHiddenApiExemptions;

    static {
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            sVmRuntime = getRuntime.invoke(null);
        } catch (Throwable e) {
            Logger.i("reflect bootstrap failed: \n"+e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean exemptAll() {
        return exempt(new String[]{"L"});
    }

    private static boolean exempt(String... methods) {
        if (sVmRuntime == null || setHiddenApiExemptions == null) {
            return false;
        }

        try {
            setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{methods});
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
