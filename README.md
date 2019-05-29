
因为工作需要Hook java中的方法，一开始用的epic，但是稳定性不太好，手动JIT编译有时会失败，导致闪退，并且9.0上hook貌似不管用。

于是就基于[AndroidMethodHook](https://github.com/panhongwei/AndroidMethodHook)做了一些修改，使得更符合Xposed的调用方式，并且做了一些精简代码。

不过正如epic的作者说的那样，这种方案不支持Hook系统的函数。因为这种方案的有一个前提就是方法调用必须是先拿到ArtMethod，再去取entrypoint然后跳转实现调用。但是很多情况下，系统知道你要调用的这个方法的entrypoint是什么，直接写死在汇编代码里，这样方法调用的时候就不会有取ArtMethod这个动作，从而不会去拿被替换的entrypoint，导致Hook失效。

不过对我来说已经够用了，最主要的是还比较稳定。


```
dependencies {
    implementation 'com.yanggs:arthook:0.2.3'
}

// 如果报错就加上这段代码
allprojects {
    repositories {
        maven{url "https://dl.bintray.com/shineygs/maven"}
    }
}
```


```

JXposed.findAndHookMethod(MainActivity.class, "testFun", String.class,int.class,new MethodCallback() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Logger.i("beforeHookedMethod:");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Logger.i("afterHookedMethod:");

    }
});     

```




