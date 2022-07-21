package com.example.forwardingiphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class Helper {
    public static String getMobileType(){
        return Build.MANUFACTURER;
    }
    /**
     * 跳转到手机系统的自启动界面或是手机管家界面
     * adb shell dumpsys activity top
     * @param context
     */
    public static void jumpStartManager(Context context) {
        Intent intent = new Intent();
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.e("StartUtils", "当前手机型号为：" + getMobileType());
            ComponentName componentName = null;
            if (getMobileType().equals("Xiaomi")) {
                // 小米 5s、红米note 4s
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
            } else if (getMobileType().equals("Letv")) {
                // 乐视2
                intent.setAction("com.letv.android.permissionautoboot");
            } else if (getMobileType().equals("samsung")) {
                //samsung Galaxy s7
                componentName = ComponentName.unflattenFromString("com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity");
                //componentName = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
            } else if (getMobileType().equals("HUAWEI")) {
                //华为 p10 android 8.0
                //componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity");
                //荣耀 H60-l03 android 4.4.2
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.optimize.bootstart.BootStartActivity");
                //componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
            } else if (getMobileType().equals("vivo")) {
                // vivo x7只能进入安全管家的界面
                componentName = ComponentName.unflattenFromString("com.iqoo.secure/.MainActivity");
                //componentName = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity");
                // vivo x7 进入手机白名单界面
                //componentName = ComponentName.unflattenFromString("com.iqoo.secure/.ui.phoneoptimize.AddWhiteListActivity");
            } else if (getMobileType().equals("Meizu")) {
                // 魅族
                componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity");
                //componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity");
            } else if (getMobileType().equals("OPPO")) {
                // OPPO R9m 可以
                componentName = ComponentName.unflattenFromString("com.coloros.safecenter/.startupapp.StartupAppListActivity");
                //componentName = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
                //耗电列表
                //componentName = ComponentName.unflattenFromString("com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
            } else if (getMobileType().equals("ulong")) {
                // 360手机
                componentName = new ComponentName("com.yulong.android.coolsafe", ".ui.activity.autorun.AutoRunListActivity");
            } else {
                // 将用户引导到系统设置页面
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
                }
            }
            intent.setComponent(componentName);
            context.startActivity(intent);
        } catch (Exception e) {
            //抛出异常就直接打开设置页面
            Log.e("StartUtils", "异常信息:" + e.getMessage());
            intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }

    public static void show(Context context,String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static String serverIp = "192.168.124.13";

    public static String webSocketBaseUrl = "http://"+ serverIp +":9003/dashboard";


}
