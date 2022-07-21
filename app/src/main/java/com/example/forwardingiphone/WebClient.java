package com.example.forwardingiphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class WebClient extends WebSocketClient {
    private static WebClient mWebClient;
    private final Context mContext;
    /**
     *  路径为ws+服务器地址+服务器端设置的子路径+参数（这里对应服务器端机器编号为参数）
     *  如果服务器端为https的，则前缀的ws则变为wss
     */
    private static final String mAddress = "ws://"+ Helper.serverIp +":9003/dashboard/websocket/";
    private void showLog(String msg){
        Log.d("WebClient---->", msg);
    }
    private WebClient(URI serverUri, Context context){
        super(serverUri, new Draft_6455());
        mContext = context;
        showLog("WebClient");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("----------" + handshakedata.getHttpStatusMessage());
        showLog("open->"+handshakedata.toString());
    }

    @Override
    public void onMessage(String message) {
        showLog("onMessage->"+message);
        sendMessageBroadcast(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        showLog("onClose->"+reason);
    }

    @Override
    public void onError(Exception ex) {
        showLog("onError->"+ex.toString());
    }

    /** 初始化
     * @param userName 用户名
     */
    public static void initWebSocket(final Context context, String userName){
        new Thread(() -> {
            try {
                mWebClient = new WebClient(new URI(mAddress + userName), context);
                try {
                    mWebClient.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** 发送消息广播
     * @param message
     */
    private void sendMessageBroadcast(String message){
        if (!message.isEmpty()){
            Map<String,Object> map = JSON.parseObject(message, Map.class);
            QlNotificationUtil.setNotificationChannel(mContext);
            QlNotificationUtil.showMuch(mContext, map.get("title").toString(),
                    map.get("message").toString(),null);
        }
    }
}
