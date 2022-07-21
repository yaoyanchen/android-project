package com.example.forwardingiphone.websoket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.forwardingiphone.Helper;
import com.example.forwardingiphone.QlNotificationUtil;
import com.example.forwardingiphone.SpUtil;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class JWebSocketClientService extends Service {
    private final String TAG="JWebSocketClientService";
    private URI uri;
    public JWebSocketClient client;
    private final JWebSocketClientBinder mBinder = new JWebSocketClientBinder();

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public JWebSocketClientService getService() {
            return JWebSocketClientService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化webSocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 初始化webSocket连接
     */
    private void initSocketClient() {
        Object user = new SpUtil(getApplicationContext(), "config").getSharedPreference("userName", "");
        URI uri = URI.create("ws://"+ Helper.serverIp +":9003/dashboard/websocket/"+user);//测试使用
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                Log.e("JWebSocketClientService", "收到的消息：" + message);
                sendMessageBroadcast(message);
//                Intent intent = new Intent();//广播接收到的消息,在Activity接收
//                intent.setAction("com.xxx.servicecallback.content");
//                intent.putExtra("message", message);
//                sendBroadcast(intent);
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                Log.e("JWebSocketClientService", "websocket连接成功");
            }
        };
        connect();
    }
    /**
     * 连接webSocket
     */
    private void connect() {
        new Thread(() -> {
            try {
                //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                client.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    //    -------------------------------------webSocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private final Handler mHandler = new Handler();
    private final Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }else {
                    //业务逻辑 这里如果服务端需要心跳包为了防止断开 需要不断发送消息给服务端
                    try {
                        while (!client.getReadyState().equals(ReadyState.OPEN)) {
                            Log.e(TAG,"连接中···请稍后");
                        }
                        client.send("");
                    }catch (Exception e) {
                        Log.e(TAG, "心跳包发送失败:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                //如果client已为空，重新初始化连接
                client = null;
                initSocketClient();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("JWebSocketClientService", "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /** 发送消息广播
     * @param message
     */
    private void sendMessageBroadcast(String message){
        if (!message.isEmpty()){
            Map<String,Object> map = JSON.parseObject(message, Map.class);
            QlNotificationUtil.setNotificationChannel(getApplicationContext());
            QlNotificationUtil.showMuch(getApplicationContext(), map.get("title").toString(),
                    map.get("message").toString(),null);
        }
    }
}
