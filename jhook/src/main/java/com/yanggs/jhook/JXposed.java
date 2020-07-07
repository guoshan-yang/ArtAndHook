package com.yanggs.jhook;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import com.yanggs.jhook.utils.JXposedHelpers;
import com.yanggs.jhook.utils.MethodUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by YangGuoShan on 2018/5/24.
 */
public class JXposed {

    private static HashMap<String,BackMethod> hooked=new HashMap();

    public static void findAndHookMethod(Class cla,String methodName,Object... parameterTypesAndCallback) {
        initContext();
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof MethodCallback))
            throw new IllegalArgumentException("no callback defined");
        try{
            MethodCallback callback = (MethodCallback) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
            Method m = cla.getDeclaredMethod(methodName, JXposedHelpers.getParameterClasses(cla.getClassLoader(), parameterTypesAndCallback));

            BackMethod back = new BackMethod();
            back.setOldMethod(m);
            back.setCallback(callback);
            beginHook(back);
        }catch (Exception e){
            e.printStackTrace();
            Log.i("JXposed",e.getClass()+":   "+e.getMessage());
            Log.d("JXposed", Log.getStackTraceString(new Throwable()));
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void findAndHookConstructor(Class<?> clazz,Object... parameterTypesAndCallback) {
        initContext();
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof MethodCallback))
            throw new IllegalArgumentException("no callback defined");
        try{
            MethodCallback callback = (MethodCallback) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
            Constructor m = clazz.getDeclaredConstructor(JXposedHelpers.getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));
            BackMethod back=new BackMethod();
            back.setOldMethod(m);
            back.setCallback(callback);
            beginHook(back);
        }catch (Exception e){
            e.printStackTrace();
            Log.i("JXposed",e.getClass()+":   "+e.getMessage());
            Log.d("JXposed", Log.getStackTraceString(new Throwable()));
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void beginHook(BackMethod backMethod) throws Exception {

        String sigName=backMethod.getOldMethod().getDeclaringClass().getSimpleName()+"_"+MethodUtil.sign(backMethod.getOldMethod());

        if(hooked.get(sigName)!=null){
            return;
        }

        if (HookUtil.isArt()){
            DexMaker dexMaker = new DexMaker();
            String className=backMethod.getOldMethod().getDeclaringClass().getName().replace(".","_");
            TypeId<?> cls = TypeId.get("L"+className+";");
            Class target = backMethod.getOldMethod().getDeclaringClass();
            if(Modifier.isFinal(target.getModifiers()) || Modifier.isFinal(backMethod.getOldMethod().getModifiers())) {
                dexMaker.declare(cls, "", Modifier.PUBLIC, TypeId.OBJECT);
            }else {
                dexMaker.declare(cls, "", Modifier.PUBLIC, TypeId.get(target));
            }
            MethodUtil.addDefaultInstanceField(dexMaker,cls);

            if(backMethod.getOldMethod() instanceof Method) {
                MethodUtil.generateMethodFromMethod(dexMaker, cls, (Method) backMethod.getOldMethod());
                MethodUtil.generateInvokerFromMethod(dexMaker, cls, (Method) backMethod.getOldMethod());
                MethodUtil.addDefaultConstructor(dexMaker, cls);
            }else {
                MethodUtil.generateMethodFromConstructor(dexMaker, cls, (Constructor) backMethod.getOldMethod());
                MethodUtil.generateInvokerFromConstructor(dexMaker, cls, (Constructor) backMethod.getOldMethod());
                int parameterCount = ((Constructor) backMethod.getOldMethod()).getParameterTypes().length;
                if (parameterCount>0){
                    MethodUtil.addDefaultConstructor(dexMaker, cls);
                }
            }

            File outputDir = new File(context.getDir("path", Context.MODE_PRIVATE).getPath());
            File outJarFile = new File(outputDir, (String) JXposedHelpers.callMethod(dexMaker,"generateFileName"));

            if (outJarFile.exists()) {
                outJarFile.delete();
            }

            ClassLoader loader = dexMaker.generateAndLoad(context.getClassLoader(), outputDir);
            Class<?> aClass = loader.loadClass(className);

            if (backMethod.getOldMethod() instanceof Method){
                Constructor con=aClass.getDeclaredConstructor();
                con.newInstance();
            }

            Member mem=null;
            Method invoker=null;

            if (!HookUtil.setMadeClassSuper(aClass)) {
                throw new FileNotFoundException("found error!");
            }

            if(backMethod.getOldMethod() instanceof Method){
                mem=aClass.getDeclaredMethod(backMethod.getOldMethod().getName(),((Method) backMethod.getOldMethod()).getParameterTypes());
                invoker=aClass.getDeclaredMethod(backMethod.getOldMethod().getName()+"_Invoker",((Method) backMethod.getOldMethod()).getParameterTypes());
            }else{
                mem=aClass.getDeclaredConstructor(((Constructor) backMethod.getOldMethod()).getParameterTypes());
                invoker=aClass.getDeclaredMethod("init_Invoker",((Constructor) backMethod.getOldMethod()).getParameterTypes());
            }

            backMethod.setInvoker(invoker);
            backMethod.setNewMethod(mem);
            HookUtil.replaceMethod(backMethod);

            hooked.put(mem.getDeclaringClass().getSimpleName()+"_"+MethodUtil.sign(backMethod.getOldMethod()),backMethod);
        }else{
            throw new RuntimeException("Only support art");
        }
    }

    public static Object invoke(String method,Object thiz,Object[] args)throws  Throwable {
        Method old=null;
        MethodCallback callback=null;
        BackMethod back =(BackMethod) hooked.get(method);
        if(back==null){
            throw new  NullPointerException("find back null");
        }
        callback=(MethodCallback) back.getCallback();
        if(callback==null){
            throw new  NullPointerException("find old Method null");
        }
        old= back.getInvoker();
        if(old==null){
            throw new  NullPointerException("find old Method null");
        }

        old.setAccessible(true);

        MethodHookParam param=new MethodHookParam();
        param.method = old;
        param.thisObject = thiz;
        param.args = args;
        try {
            callback.beforeHookedMethod(param);
        } catch (Throwable t) {
            // reset result (ignoring what the unexpectedly exiting callback did)
            t.printStackTrace();
            param.setResult(null);
            param.returnEarly = false;
        }
        if (param.getThrowable()!=null) {
            throw param.getThrowable();
        }
        if (param.returnEarly) {
            return param.getResult();
        }

        Object res = old.invoke(thiz, args);
        param.setResult(res);
        try {
            callback.afterHookedMethod(param);
        } catch (Throwable t) {
            param.setResult(null);
        }
        if (param.getThrowable()!=null) {
            throw param.getThrowable();
        }
        return param.getResult();
    }

    private static Context context=null;

    public static Context initContext() {
        if (context == null) {
            try {
                Class at = Class.forName("android.app.ActivityThread");
                Application current = (Application)at.getDeclaredMethod("currentApplication").invoke(null);
                context = current.getBaseContext();
                return context;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return context;
        }
    }

}
