package com.iwcode.jhook.utils;

import com.iwcode.jhook.MethodCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JXposedHelpers {

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        if (classLoader == null)
            classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        try {
            Constructor<?> constructor = findConstructor(clazz, getParameterTypes(args));
            return constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(String clazz, Object... args) {
        try {
            Constructor<?> constructor = findConstructor(Class.forName(clazz), getParameterTypes(args));
            return constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(String clazz, Class<?>[] types, Object... args) throws Exception {
        Constructor<?> constructor = findConstructor(Class.forName(clazz), types);
        return constructor.newInstance(args);
    }

    public static Class findClass(String clazz) throws ClassNotFoundException {
        return Class.forName(clazz);
    }

    public static Object callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = findMethod(obj.getClass(), methodName, parameterTypes);
            return method.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object callMethod(Object obj, String methodName, Object... args) {
        try {
            Method method = findMethod(obj.getClass(), methodName, getParameterTypes(args));
            return method.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Method method = findMethod(clazz, methodName, getParameterTypes(args));
            return method.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = findMethod(clazz, methodName, parameterTypes);
            return method.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object callStaticMethod(String clazz, String methodName, Object... args) {
        try {
            Method method = findMethod(Class.forName(clazz), methodName, getParameterTypes(args));
            return method.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setObjectField(Object obj, String fieldName, Object value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void setIntField(Object obj, String fieldName, int value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.setInt(obj, value);
    }

    public static void setFloatField(Object obj, String fieldName, float value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.setFloat(obj, value);
    }

    public static void setLongField(Object obj, String fieldName, long value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.setLong(obj, value);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.setBoolean(obj, value);
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getLongField(Object obj, String fieldName) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        return field.getLong(obj);
    }

    public static int getIntField(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return field.getInt(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        try {
            Field field = findField(clazz, fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class))
                    break;
                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
        }

        Method bestMatch = null;
        Class<?> clz = clazz;
        boolean considerPrivateMethods = true;
        do {
            for (Method method : clz.getDeclaredMethods()) {
                if (!considerPrivateMethods && Modifier.isPrivate(method.getModifiers()))
                    continue;

                if (method.getName().equals(methodName) && ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                    if (bestMatch == null || MemberUtils.comparemContexteterTypes(
                            method.getParameterTypes(),
                            bestMatch.getParameterTypes(),
                            parameterTypes) < 0) {
                        bestMatch = method;
                    }
                }
            }
            considerPrivateMethods = false;
        } while ((clz = clz.getSuperclass()) != null);

        if (bestMatch != null) {
            bestMatch.setAccessible(true);
            return bestMatch;
        } else {
            NoSuchMethodError e = new NoSuchMethodError(clazz.getName() + "#" + methodName);
            throw e;
        }
    }

    private static Constructor<?> findConstructor(Class<?> clazz, Class<?>[] parameterTypes) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
        }

        Constructor<?> bestMatch = null;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> con : constructors) {
            if (ClassUtils.isAssignable(parameterTypes, con.getParameterTypes(), true)) {
                if (bestMatch == null || MemberUtils.comparemContexteterTypes(
                        con.getParameterTypes(),
                        bestMatch.getParameterTypes(),
                        parameterTypes) < 0) {
                    bestMatch = con;
                }
            }
        }
        if (bestMatch != null) {
            bestMatch.setAccessible(true);
            return bestMatch;
        } else {
            NoSuchMethodError e = new NoSuchMethodError(clazz.getName());
            throw e;
        }
    }

    private static Class<?>[] getParameterTypes(Object... args) {
        Class<?>[] clazzes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            clazzes[i] = (args[i] != null) ? args[i].getClass() : null;
        }
        return clazzes;
    }

    public static Class<?>[] getParameterClasses(ClassLoader classLoader, Object[] parameterTypesAndCallback) {
        Class<?>[] parameterClasses = null;
        for (int i = parameterTypesAndCallback.length - 1; i >= 0; i--) {
            Object type = parameterTypesAndCallback[i];
            if (type == null)
                throw new NullPointerException("parameter type must not be null");

            // ignore trailing callback
            if (type instanceof MethodCallback)
                continue;

            if (parameterClasses == null)
                parameterClasses = new Class<?>[i+1];

            if (type instanceof Class)
                parameterClasses[i] = (Class<?>) type;
            else if (type instanceof String) {
                try {
                    parameterClasses[i] = classLoader.loadClass((String) type); //((String) type, classLoader);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else
                throw new NullPointerException("parameter type must either be specified as Class or String");
        }

        // if there are no arguments for the method
        if (parameterClasses == null)
            parameterClasses = new Class<?>[0];

        return parameterClasses;
    }
}
