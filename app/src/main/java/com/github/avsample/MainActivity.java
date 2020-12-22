package com.github.avsample;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 1、加载native库
    static {
        System.loadLibrary("native-lib");
    }

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //1、将Java数据传输到native
        TextView tv = findViewById(R.id.sample_text);
        test1(true, (byte) 1, ',', (short) 3, 4l, 4.5f, 2.666d, "klaus", 18,
                new int[]{1, 2, 3, 4, 5},
                new String[]{"a", "b", "c"},
                new boolean[]{false, true});
        //2、处理JAva对象
        String str = getPerson().toString();
        tv.setText(str);

//        dynamicRegister("我是动态注册的");
//        dynamicRegister2("异常测试");
//        test3(tv);
        test4(tv);
        /**
         * 调用native 函数
         */
//        tv.setText(stringFromJNI());
    }

    /**
     * 2、定义native函数
     */
    public native String stringFromJNI();

    /***
     * 把Java 的基本类型传入到native中
     */
    public native void test1(boolean b, byte b1, char c, short s, long l, float f, double d, String name, int age, int[] i,
                             String[] strs, boolean[] array);

    public native Person getPerson();

    public native void dynamicRegister(String name);

    public native void dynamicRegister2(String name);

    private void testException() throws NullPointerException {
        throw new NullPointerException("MainActivity testException NullPointerException");
    }

    public void test3(View view) {
        test4();
    }

    public native void test4();

    public void test4(View view) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    count();
                    nativeCount();
                }
            }).start();
        }
    }

    private void count() {
        synchronized (this) {
            count++;
            Log.d("java", "count=" + count);
        }
    }

    public native void nativeCount();

    public void test5(View view){
        testThread();
    }

    public void updateUI(){
        if (Looper.getMainLooper() == Looper.myLooper()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("UI")
                    .setMessage("native 运行在主线程，直接更新UI")
                    .setPositiveButton("确认",null)
                    .show();
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("UI")
                            .setMessage("native运行在子线程切换为主线程 更新UI")
                            .setPositiveButton("确认",null)
                            .show();
                }
            });
        }
    }
    public native void testThread();

    public native void unThread();

}
