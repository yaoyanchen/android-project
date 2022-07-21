package com.example.forwardingiphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.forwardingiphone.model.SendMsg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NoteReceiver extends BroadcastReceiver {
    private static MessageListener mMessageListener;
    private final String TAG = "NoteReceiver";
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    public NoteReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for(Object pdu:pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte [])pdu);
                String sender = smsMessage.getDisplayOriginatingAddress();
                //短信内容
                String content = smsMessage.getDisplayMessageBody();
                long date = smsMessage.getTimestampMillis();
                Date tiemDate = new Date(date);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = simpleDateFormat.format(tiemDate);

                //过滤不需要读取的短信的发送号码
                if ("+8613450214963".equals(sender)) {
                    mMessageListener.onReceived(content);
                    abortBroadcast();
                }

                new Thread(() -> {
                    Object username = new SpUtil(context, "config").getSharedPreference("userName", "");

                    SendMsg jsonParam = new SendMsg();
                    jsonParam.setTitle(username.toString());
                    String msg = "来自:" + sender + "的消息:" + content;
                    jsonParam.setMessage(msg);
                    jsonParam.setUserName("system");
                    try {
                        OkHttpUtils.sendPost(Helper.webSocketBaseUrl + "/api/notification/push", JSON.toJSONString(jsonParam), new HashMap<>(), new HashMap<>());
                        Log.e(TAG, "短信消息推送成功...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

    }

    //回调接口
    public interface MessageListener {
        public void onReceived(String message);
    }

    public void setOnReceivedMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }
}
