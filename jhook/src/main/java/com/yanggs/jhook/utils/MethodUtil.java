package com.yanggs.jhook.utils;

import android.util.Log;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.FieldId;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.yanggs.jhook.JXposed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by YangGuoShan on 2018/5/27.
 */
public class MethodUtil {

    private static TypeId<MethodUtil> utilType = TypeId.get(MethodUtil.class);
    private static TypeId<Integer> IntegerType = TypeId.get(Integer.class);
    private static TypeId<Long> LongType = TypeId.get(Long.class);
    private static TypeId<Short> ShortType = TypeId.get(Short.class);
    private static TypeId<Double> DoubleType = TypeId.get(Double.class);
    private static TypeId<Boolean> BooleanType = TypeId.get(Boolean.class);
    private static TypeId<Float> FloatType = TypeId.get(Float.class);
    private static TypeId<Byte> ByteType= TypeId.get(Byte.class);
    private static TypeId<Character> CharacterType= TypeId.get(Character.class);

    private static MethodId Integer_value=IntegerType.getMethod(IntegerType,"valueOf",TypeId.INT);
    private static MethodId Long_value=LongType.getMethod(LongType,"valueOf",TypeId.LONG);
    private static MethodId Short_value=ShortType.getMethod(ShortType,"valueOf",TypeId.SHORT);
    private static MethodId Double_value=DoubleType.getMethod(DoubleType,"valueOf",TypeId.DOUBLE);
    private static MethodId Boolean_value=BooleanType.getMethod(BooleanType,"valueOf",TypeId.BOOLEAN);
    private static MethodId Float_value=FloatType.getMethod(FloatType,"valueOf",TypeId.FLOAT);
    private static MethodId Byte_value=ByteType.getMethod(ByteType,"valueOf",TypeId.BYTE);
    private static MethodId Char_value=CharacterType.getMethod(CharacterType,"valueOf",TypeId.CHAR);

    private static MethodId int_value=IntegerType.getMethod(TypeId.INT,"intValue");
    private static MethodId long_value=LongType.getMethod(TypeId.LONG,"longValue");
    private static MethodId short_value=ShortType.getMethod(TypeId.SHORT,"shortValue");
    private static MethodId double_value=DoubleType.getMethod(TypeId.DOUBLE,"doubleValue");
    private static MethodId boolean_value=BooleanType.getMethod(TypeId.BOOLEAN,"booleanValue");
    private static MethodId float_value=FloatType.getMethod(TypeId.FLOAT,"floatValue");
    private static MethodId byte_value=ByteType.getMethod(TypeId.BYTE,"byteValue");
    private static MethodId char_value=CharacterType.getMethod(TypeId.CHAR,"charValue");

    public static void addDefaultInstanceField(DexMaker dexMaker, TypeId<?> declaringType) {
        FieldId fieldId=declaringType.getField(TypeId.INT,"flag");
        dexMaker.declare(fieldId,Modifier.PUBLIC ,null);
    }

    public static void addDefaultConstructor(DexMaker dexMaker, TypeId<?> declaringType) {
        Code code = dexMaker.declare(declaringType.getConstructor(), Modifier.PUBLIC);
        Local<?> thisRef = code.getThis(declaringType);
        code.invokeDirect(TypeId.OBJECT.getConstructor(), null, thisRef);
        code.returnVoid();
    }

    public static void generateMethodFromMethod(DexMaker dexMaker, TypeId<?> declaringType,Method m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName(), params);
        Code code;
        if(Modifier.isStatic(m.getModifiers())) {
            code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
        }else{
            int mode=Modifier.isPrivate(m.getModifiers())?Modifier.PRIVATE:Modifier.PUBLIC;
            code = dexMaker.declare(proxy, mode);
        }
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local i_ = code.newLocal(TypeId.INT);
        Local arg_ = code.newLocal(TypeId.OBJECT);

        Local localResult = code.newLocal(TypeId.OBJECT);
        Local caller = code.newLocal(TypeId.STRING);
        Local res = code.newLocal(TypeId.get(m.getReturnType()));
        Local cast = m.getReturnType().equals(void.class)?null:code.newLocal(getClassTypeFromClass(m.getReturnType()));
        Local<?> thisRef ;
        if(Modifier.isStatic(m.getModifiers())){
            thisRef=code.newLocal(declaringType);
            code.loadConstant(thisRef,null);
        }else{
            thisRef=code.getThis(declaringType);
        }
        code.loadConstant(caller,m.getDeclaringClass().getName().replace(".","_")+"_"+sign(m));
        code.loadConstant(a, proxy.getParameters().size() );
        code.newArray(args, a);
        for (int i = 0; i < pTypes.length; ++i) {
            code.loadConstant(i_, i);
            MethodId mId = getValueFromClass(pTypes[i]);
            if (mId != null) {
                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
                code.aput(args, i_, arg_);
            } else {
                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
            }
        }
        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke",TypeId.STRING, TypeId.OBJECT, TypeId.get(Object[].class));
        code.invokeStatic(invoke, localResult,caller, thisRef, args);
        if(m.getReturnType().equals(void.class)){
            code.returnVoid();
            return;
        }
        if(getValueFromClass(m.getReturnType())!=null){
            MethodId mId = toValueFromClass(m.getReturnType());
            code.cast(cast,localResult);
            code.invokeVirtual(mId,res, cast);
            code.returnValue(res);
            return;
        }else{
            code.cast(res,localResult);
            code.returnValue(res);
            return;
        }
    }


    public static void generateInvokerFromMethod(DexMaker dexMaker, TypeId<?> declaringType,Method m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName()+"_Invoker", params);
        Code code;
        if(Modifier.isStatic(m.getModifiers())) {
            code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
        }else{
            int mode=Modifier.isPrivate(m.getModifiers())?Modifier.PRIVATE:Modifier.PUBLIC;
            code = dexMaker.declare(proxy, mode);
        }
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local i_ = code.newLocal(TypeId.INT);
        Local arg_ = code.newLocal(TypeId.OBJECT);

        Local localResult = code.newLocal(TypeId.OBJECT);
        Local caller = code.newLocal(TypeId.STRING);
        Local res = code.newLocal(TypeId.get(m.getReturnType()));
        Local cast = m.getReturnType().equals(void.class)?null:code.newLocal(getClassTypeFromClass(m.getReturnType()));
        Local<?> thisRef ;
        if(Modifier.isStatic(m.getModifiers())){
            thisRef=code.newLocal(declaringType);
            code.loadConstant(thisRef,null);
        }else{
            thisRef=code.getThis(declaringType);
        }
        code.loadConstant(caller,m.getDeclaringClass().getName().replace(".","_")+"_"+sign(m));
        code.loadConstant(a, proxy.getParameters().size() );
        code.newArray(args, a);
        for (int i = 0; i < pTypes.length; ++i) {
            code.loadConstant(i_, i);
            MethodId mId = getValueFromClass(pTypes[i]);
            if (mId != null) {
                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
                code.aput(args, i_, arg_);
            } else {
                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
            }
        }
        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke",TypeId.STRING, TypeId.OBJECT, TypeId.get(Object[].class));
        code.invokeStatic(invoke, localResult,caller, thisRef, args);
        if(m.getReturnType().equals(void.class)){
            code.returnVoid();
            return;
        }
        if(getValueFromClass(m.getReturnType())!=null){
            MethodId mId = toValueFromClass(m.getReturnType());
            code.cast(cast,localResult);
            code.invokeVirtual(mId,res, cast);
            code.returnValue(res);
            return;
        }else{
            code.cast(res,localResult);
            code.returnValue(res);
            return;
        }
    }

    public static void generateMethodFromConstructor(DexMaker dexMaker, TypeId<?> declaringType, Constructor m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
//            params[0] = TypeId.OBJECT;
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getConstructor( params);
        Code code = dexMaker.declare(proxy, Modifier.PUBLIC);
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local i_ = code.newLocal(TypeId.INT);
        Local arg_ = code.newLocal(TypeId.OBJECT);
        Local localResult = code.newLocal(TypeId.OBJECT);
//        Local<Object> obj=code.newLocal(TypeId.OBJECT);
        Local<?> thisRef = code.getThis(declaringType);
        Local caller = code.newLocal(TypeId.STRING);
        code.loadConstant(caller,m.getDeclaringClass().getName().replace(".","_")+"_"+sign(m));
        code.invokeDirect(TypeId.OBJECT.getConstructor(), null, thisRef);
//        code.loadConstant(thisRef,code.getThis(declaringType));
        code.loadConstant(a, proxy.getParameters().size() );
        code.newArray(args, a);
        for (int i = 0; i < pTypes.length; ++i) {
            code.loadConstant(i_, i);
            MethodId mId = getValueFromClass(pTypes[i]);
            if (mId != null) {
                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
                code.aput(args, i_, arg_);
            } else {
                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
            }
        }
        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke",TypeId.STRING, TypeId.OBJECT, TypeId.get(Object[].class));
        code.invokeStatic(invoke, localResult,caller, thisRef, args);
        code.returnVoid();
    }

    public static void generateInvokerFromConstructor(DexMaker dexMaker, TypeId<?> declaringType,Constructor m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getMethod(TypeId.get(Void.TYPE), "init_Invoker", params);
        Code code;
        code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
        code.returnVoid();
        return;

    }

    public static Object invoke(String caller,Object thiz,Object[] args)throws Throwable{
//        Logger.i("MethodUtil->invoke: "+caller+","+thiz+","+args[0]+","+args[1]);
        return JXposed.invoke(caller, thiz, args);
    }

    private static String queryMethodShorty(Member m){
        String mShort="";
        Type[] types=null;
        if(m instanceof Method){
            types=((Method)m).getParameterTypes();
        }else if(m instanceof Constructor){
            types=((Constructor)m).getParameterTypes();
        }
        for(Type c:types){
            String name=c.toString();
            if(name.equals(int.class.getName())){
                mShort=mShort+"I";
            }else if(name.equals(byte.class.getName())){
                mShort=mShort+"B";
            }else if(name.equals(char.class.getName())){mShort=mShort+"C";}
            else if(name.equals(short.class.getName())){mShort=mShort+"S";}
            else if(name.equals(float.class.getName())){mShort=mShort+"F";}
            else if(name.equals(boolean.class.getName())){mShort=mShort+"Z";}
            else if(name.equals(long.class.getName())){mShort=mShort+"J";}
            else if(name.equals(double.class.getName())){mShort=mShort+"D";}
            else if(name.startsWith("[")){mShort=mShort+"L";}
            else {mShort=mShort+"L";}
        }
        return mShort;
    }

    private static String queryReturnType(Member m){
        String mShort="";
        if(m instanceof Method){
            Type rty=((Method)m).getReturnType();
            String name=rty.toString();
            if(name.equals(int.class.getName())){
                mShort=mShort+"I";
            }else if(name.equals(byte.class.getName())){
                mShort=mShort+"B";
            }else if(name.equals(char.class.getName())){mShort=mShort+"C";}
            else if(name.equals(short.class.getName())){mShort=mShort+"S";}
            else if(name.equals(float.class.getName())){mShort=mShort+"F";}
            else if(name.equals(boolean.class.getName())){mShort=mShort+"Z";}
            else if(name.equals(long.class.getName())){mShort=mShort+"J";}
            else if(name.equals(double.class.getName())){mShort=mShort+"D";}
            else {mShort=mShort+"L";}
            return mShort;
        }else if(m instanceof Constructor){
            //((Constructor)m).;
            return "L";
        }
        return  "L";
    }

    /**
     * 获取函数签名
     * @param m
     * @return
     */
    public static String sign(Member m){
        String shorty=queryMethodShorty(m);
        String ret=queryReturnType(m);
        if(m instanceof Method){
            return m.getName()+shorty+ret;
        }else {
            return shorty + ret;
        }
    }

    private static TypeId getTypeIdFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return TypeId.INT;
        }else if(cls.getName().equals(long.class.getName())){
            return TypeId.LONG;
        }else if(cls.getName().equals(short.class.getName())){
            return TypeId.SHORT;
        }else if(cls.getName().equals(double.class.getName())){
            return TypeId.DOUBLE;
        }else if(cls.getName().equals(boolean.class.getName())){
            return TypeId.BOOLEAN;
        }else if(cls.getName().equals(float.class.getName())){
            return TypeId.FLOAT;
        }else if(cls.getName().equals(byte.class.getName())){
            return TypeId.BYTE;
        }else if(cls.getName().equals(char.class.getName())){
            return TypeId.CHAR;
        }else if(cls.getName().equals(void.class.getName())){
            return TypeId.VOID;
        }else{
            return  TypeId.get(cls);
        }
    }
    private static TypeId getClassTypeFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return IntegerType;
        }else if(cls.getName().equals(long.class.getName())){
            return LongType;
        }else if(cls.getName().equals(short.class.getName())){
            return ShortType;
        }else if(cls.getName().equals(double.class.getName())){
            return DoubleType;
        }else if(cls.getName().equals(boolean.class.getName())){
            return BooleanType;
        }else if(cls.getName().equals(float.class.getName())){
            return FloatType;
        }else if(cls.getName().equals(byte.class.getName())){
            return BooleanType;
        }else if(cls.getName().equals(char.class.getName())){
            return CharacterType;
        }
        return TypeId.get(cls);
    }
    private static MethodId getValueFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return Integer_value;
        }else if(cls.getName().equals(long.class.getName())){
            return Long_value;
        }else if(cls.getName().equals(short.class.getName())){
            return Short_value;
        }else if(cls.getName().equals(double.class.getName())){
            return Double_value;
        }else if(cls.getName().equals(boolean.class.getName())){
            return Boolean_value;
        }else if(cls.getName().equals(float.class.getName())){
            return Float_value;
        }else if(cls.getName().equals(byte.class.getName())){
            return Byte_value;
        }else if(cls.getName().equals(char.class.getName())){
            return Char_value;
        }else{
            return  null;
        }
    }
    private static MethodId toValueFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return int_value;
        }else if(cls.getName().equals(long.class.getName())){
            return long_value;
        }else if(cls.getName().equals(short.class.getName())){
            return short_value;
        }else if(cls.getName().equals(double.class.getName())){
            return double_value;
        }else if(cls.getName().equals(boolean.class.getName())){
            return boolean_value;
        }else if(cls.getName().equals(float.class.getName())){
            return float_value;
        }else if(cls.getName().equals(byte.class.getName())){
            return byte_value;
        }else if(cls.getName().equals(char.class.getName())){
            return char_value;
        }else{
            return  null;
        }
    }
}
