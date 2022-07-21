package com.example.forwardingiphone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.forwardingiphone.model.SendMsg;
import com.example.forwardingiphone.websoket.JWebSocketClientService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity {

    public String receiverMsg;
    SmsReceiver receiver;
    IntentFilter filter;
    class GetOnlineUserSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            receiverMsg = adapterView.getItemAtPosition(i).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void initSpinner(Context context) {
        new Thread(() -> {
            try {
                String msg = OkHttpUtils.get(Helper.webSocketBaseUrl + "/api/notification/getOnlineUser", new HashMap<>(), new HashMap<>());
                List<String> data = JSON.parseArray(msg, String.class);
                String[] array = data.toArray(new String[0]);
                runOnUiThread(() -> {
                    //声明一个下拉列表的数组适配器
                    ArrayAdapter<String> starAdapter = new ArrayAdapter<>(context, R.layout.item_select, array);
                    //设置数组适配器的布局样式
                    starAdapter.setDropDownViewResource(R.layout.item_drapdown);
                    //从布局文件中获取名叫sp_dialog的下拉框
                    Spinner sp = findViewById(R.id.spinner);
                    //设置下拉框的标题，不设置就没有难看的标题了
                    sp.setPrompt("请选择配送方式");
                    //设置下拉框的数组适配器
                    sp.setAdapter(starAdapter);
                    //设置下拉框默认的显示第一项
                    sp.setSelection(0);
                    //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
                    sp.setOnItemSelectedListener(new GetOnlineUserSpinnerListener());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private EditText editText;
    private EditText urlEditText;
    private EditText userNameEditText;
    private EditText sendContentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        Button button8 = findViewById(R.id.button8);
        Button button9 = findViewById(R.id.button3);
        Button button10 = findViewById(R.id.button9);
        Button button11 = findViewById(R.id.button10);
        editText = findViewById(R.id.editTextTextMultiLine);
        urlEditText = findViewById(R.id.editTextTextPersonName);
        userNameEditText = findViewById(R.id.editTextTextPersonName2);
        sendContentEditText = findViewById(R.id.editTextTextPersonName4);
        filter=new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED" );
        receiver=new SmsReceiver();
        registerReceiver(receiver,filter);//注册广播接收器

        button10.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, MyNotificationListenerService.class);//启动服务
//            startService(intent);
            toggleNotificationListenerService(this);
        });
        button11.setOnClickListener(v -> {
                    initSpinner(this);
                }
        );


        button.setOnClickListener(v -> {
            String msg = editText.getText().toString();
            if (TextUtils.isEmpty(msg.trim())) {
                show("发送能容不能为空");
                return;
            }
            new Thread(() -> {
                String baseUrl = urlEditText.getText().toString();
                try {
                    Map<String, Object> param = new HashMap<>();
                    param.put("icon", "http://81.68.206.166/icon-google.png");
                    param.put("copy", msg);
                    param.put("autoCopy", "1");
                    param.put("sound", "birdsong");
                    OkHttpUtils.get(baseUrl + "Android/" + msg, param, new HashMap<>());
                    runOnUiThread(() ->
                            {
                                show("发送成功");
                                editText.setText("");
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
        button2.setOnClickListener(v -> {
            if (notificationListenerEnable()) {
                show("通知监听已开启,无法重复开启");
                return;
            }
            //打开监听引用消息Notification access
            Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent_s);
        });
        String baseUrl = new SpUtil(getApplicationContext(), "config").getSharedPreference("baseUrl", "").toString();
        if (!baseUrl.equals("")) {
            urlEditText.setText(baseUrl);
        }
        String userName = new SpUtil(getApplicationContext(), "config").getSharedPreference("userName", "").toString();
        if (!userName.equals("")) {
            userNameEditText.setText(userName);
        }
        button4.setOnClickListener(v -> {
            new SpUtil(getApplicationContext(), "config").put("baseUrl", urlEditText.getText().toString());
            new SpUtil(getApplicationContext(), "config").put("userName", userNameEditText.getText().toString());
            show("保存成功");
        });

//        if (!notificationListenerEnable()) {
//            show("请打开通知权限");
//            Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
//            startActivity(intent_s);
//        }
//        startKeepAliveJobService(this);
        //button5点击事件允许后台运行
        button5.setOnClickListener(v -> {
            if (isIgnoringBatteryOptimizations()) {
                show("已经允许后台运行");
            } else {
                requestIgnoreBatteryOptimizations();
            }
        });
        //button6点击弹出选择文件对话框
        button6.setOnClickListener(v -> {
            chooseFile();
        });
        button7.setOnClickListener(v -> {
            String aa = userNameEditText.getText().toString();
            //判断aa是否为中文
            if (aa.matches("[\u4e00-\u9fa5]+")) {
                show("用户名不能为中文");
                return;
            }
            startService(new Intent(this, JWebSocketClientService.class));
//            WebClient.initWebSocket(this, aa);
        });
        button8.setOnClickListener(v -> {
            Helper.jumpStartManager(this);
        });
        button9.setOnClickListener(v -> {
            //receiveUserNameEditText不能为空
            if (receiverMsg != null && TextUtils.isEmpty(receiverMsg.trim())) {
                show("接收人不能为空");
                return;
            }
            //sendContentEditText不能为空
            if (TextUtils.isEmpty(sendContentEditText.getText().toString().trim())) {
                show("发送内容不能为空");
                return;
            }
            new Thread(() -> {
                SendMsg sendMsg = new SendMsg(userNameEditText.getText().toString(), sendContentEditText.getText().toString(), receiverMsg);
                try {
                    String msg = OkHttpUtils.sendPost(Helper.webSocketBaseUrl + "/api/notification/push",
                            JSON.toJSONString(sendMsg),
                            new HashMap<>(),
                            new HashMap<>()
                    );
                    SendMsg result = JSON.parseObject(msg, SendMsg.class);
                    if (result != null && result.getCode() == 500) {
                        QlNotificationUtil.setNotificationChannel(this);
                        QlNotificationUtil.show(this, "发送失败", result.getMessage(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }


    private boolean notificationListenerEnable() {
        boolean enable = false;
        String packageName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            enable = flat.contains(packageName);
        }
        return enable;
    }

    private void startKeepAliveJobService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(245, new ComponentName(context, KeepAliveJobService.class));
            builder.setPeriodic(6 * 60 * 1000);
            //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
            }
            builder.setPersisted(true);
            JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            if (scheduler != null) {
                scheduler.schedule(builder.build());
            }
        }
    }

    //先关闭再启动
    public static void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, MyNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, MyNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public void show(String content) {
        Helper.show(this, content);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //**********************************File Choose Depart****************************************

    private static final String TAG1 = "FileChoose";

    // 调用系统文件管理器
    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choose File"), CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private static final int CHOOSE_FILE_CODE = 0;

    @Override
// 文件选择完之后，自动调用此函数
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
                if (Uri2PathUtil.getRealPathFromUri(this, uri) != null) {
                    //从uri得到绝对路径，并获取到file文件
                    File file = new File(Uri2PathUtil.getRealPathFromUri(this, uri));
                    new Thread(() -> {
                        try {
                            String msg = OkHttpUtils.postFile("http://81.68.206.166/mbjyw_px/api/util/upload", new HashMap<>(), new HashMap<>(), file);
                            AjaxResult o = JSON.parseObject(msg, AjaxResult.class);
                            if (o.getCode() == 0) {
                                runOnUiThread(() -> {
                                    show("发送成功");
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                } else {
                    Log.e("fileUpload", "获取文件失败");
                }
            }
        } else {
            Log.e(TAG1, "onActivityResult() error, resultCode: " + resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            StringBuilder content = new StringBuilder();//用于存储短信内容
            String sender = null;//存储短信发送方手机号
            Bundle bundle = intent.getExtras();//通过getExtras()方法获取短信内容
            String format = intent.getStringExtra("format");
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");//根据pdus关键字获取短信字节数组，数组内的每个元素都是一条短信
                for (Object object : pdus) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) object, format);//将字节数组转化为Message对象
                    sender = message.getOriginatingAddress();//获取短信手机号
                    content.append(message.getMessageBody());//获取短信内容
                }
            }
            Object username = new SpUtil(getApplicationContext(), "config").getSharedPreference("userName", "");

            new Thread(() -> {
                Map<String,Object> param = new HashMap<>();
                param.put("title",username.toString());
                param.put("userName","email");
                param.put("message", content);
                try {
                    OkHttpUtils.sendPost(Helper.webSocketBaseUrl + "/api/notification/push",
                            JSON.toJSONString(param),
                            new HashMap<>(),
                            new HashMap<>());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date date = new Date();
                Log.i("okHttp","时间:" + sdf.format(date) +",通知发送成功,内容是:"+content);
            }).start();
        }
    }
}