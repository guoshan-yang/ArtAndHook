package com.iwcode;


import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static final String TAG = "JXposed";

    public static final String logPath = "mnt/sdcard/Android";

    public static void i(final Object o) {
        if (o != null){
            Log.i(TAG,o.toString());
//            write(TAG+" : "+o.toString());
        }else{
            Log.i(TAG,"null");
//            write(TAG+" : null");
        }
    }

    public static void i(String tag,final Object o) {
        if (o != null){
            Log.i(tag,o.toString());
//            write(tag+" : "+o.toString());
        }else{
            Log.i(tag,"null");
//            write(tag+" : null");
        }
    }

    private static void write(String log){
        File configDir = new File(logPath, "vkwechat-"+getDay()+".log");
        try {
            FileWriter writer = new FileWriter(configDir, true);
            writer.append("["+getTime()+"] "+log);
            writer.flush();
            writer.append("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTime(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        return format.format(date);
    }

    private static String getDay(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(date);
    }

    public static void clearCacheLog(){
        File[] files = new File(logPath).listFiles((dir, name) -> name.endsWith(".log"));

        for (int i = 0; i < files.length; i++){
            long lastModified = files[i].lastModified();
            if (lastModified < getWeekAgo()){
                files[i].delete();
                Logger.i("delete log file = "+files[i].getName());
            }
        }
    }

    public static long getWeekAgo(){
        long timeMillis = System.currentTimeMillis();
        timeMillis  = timeMillis - 1000*60*60*24*7;
        return timeMillis;
    }

    public static long getMonthAgo(){
        long timeMillis = System.currentTimeMillis();
        timeMillis  = timeMillis - 1000*60*60*24*30;
        return timeMillis;
    }
}
