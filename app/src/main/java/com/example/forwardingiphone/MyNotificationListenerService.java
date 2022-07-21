package com.example.forwardingiphone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("OverrideAbstract")
public class MyNotificationListenerService extends NotificationListenerService {
    private static boolean serviceIsLive;
    private BufferedWriter bw;
    private SimpleDateFormat sdf;
    private final MyHandler handler = new MyHandler();
    private String data = "";

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
//            String msgString = (String) msg.obj;
//            Toast.makeText(getApplicationContext(), msgString, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //获取服务通知
//        Notification notification = createForegroundNotification();
        //将服务设置与启动状态
//        startForeground(28374, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("KEVIN", "通知栏监听服务启动" + "-----------");
        data = intent.getStringExtra("data");
        return super.onStartCommand(intent, flags, startId);
    }

    //服务停止时重新启动该服务
    @Override
    public void onDestroy() {
        Log.i("KEVIN", "通知栏监听服务停止" + "-----------");
        Intent intent = new Intent(this, MyNotificationListenerService.class);
        startService(intent);
        MyNotificationListenerService.serviceIsLive = false;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //        super.onNotificationPosted(sbn);
        try {
            //有些通知不能解析出TEXT内容，这里做个信息能判断
            if (sbn.getNotification().tickerText != null) {
                SharedPreferences sp = getSharedPreferences("msg", MODE_PRIVATE);
                String nMessage = sbn.getNotification().tickerText.toString();
                Log.e("KEVIN", "Get Message" + "-----" + nMessage);
                sp.edit().putString("getMsg", nMessage).apply();
                Message obtain = Message.obtain();
                obtain.obj = nMessage;
                mHandler.sendMessage(obtain);
                Message message = handler.obtainMessage();
                message.what = 1;
                handler.sendMessage(message);
                getNotifyData(sbn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MyNotificationListenerService.this, "发生异常:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void getNotifyData(StatusBarNotification sbn){
        try {
            Bundle extras = sbn.getNotification().extras;
            String content;
            String title = extras.getString(Notification.EXTRA_TITLE,""); //通知标题
            content = extras.getString(Notification.EXTRA_TEXT,"");//通知内容
            Object baseUrl = new SpUtil(getApplicationContext(), "config").getSharedPreference("baseUrl", "");
            new Thread(() -> {
                try {
                    Map<String,Object> param = new HashMap<>();
                    param.put("icon","http://81.68.206.166/icon-google.png");
                    param.put("group","安卓通知");
                    OkHttpUtils.get(baseUrl.toString() + title +"/" + content, param,new HashMap<>());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = new Date();
                    Log.i("okHttp","时间:" + sdf.format(date) +",通知发送成功,内容是:"+content);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("okhttp","通知转发失败:" + e.getMessage());
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Notification createForegroundNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "channel_id";
        //新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 用户可见通道
            String channelName = "前台服务显示";
            //通道重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("描述");
            //LED灯闪烁
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            //震动
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(channel);
        }
        //创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        //通知大图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
        //通知小图标
        builder.setSmallIcon(R.drawable.icon);
        //通知标题
        builder.setContentTitle("监听通知服务正在运行");
        //通知内容
        builder.setContentText("监听中...");
        //设置通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //设定启动的内容
        Intent intent = new Intent(this, MyNotificationListenerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        //创建通知并返回
        return builder.build();
    }



    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
//                    Toast.makeText(MyService.this,"Bingo",Toast.LENGTH_SHORT).show();
            }
        }

    }
}
