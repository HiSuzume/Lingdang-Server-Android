package github.suzume.foreground.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.app.Activity;

/**
 * @Author 铃芽
 * @Date 2023/05/09 18:12
 * @Describe 前台服务基类
 */
public abstract class ForegroundService extends Service {
    protected static boolean service_started = false;
    protected static ForegroundService self;

    @Override
    public abstract IBinder onBind(Intent intent);

    public static void start(Context c, Class<?> cl) {
        if (!service_started) {
            if (Build.VERSION.SDK_INT >= 26)
                c.startForegroundService(new Intent(c, cl));
            c.startService(new Intent(c, cl));
        }
    }

    public static void stop() {
        if (service_started && self != null)
            self.stopSelf();
    }

    @Override
    public void onCreate() {
        self = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!service_started) {
            onCreateForegroundNotification();
        }
        service_started = true;
        return super.onStartCommand(intent, flags, startId);
    }

    public abstract void onCreateForegroundNotification();

    @Override
    public void onDestroy() {
        service_started = false;
        stopForeground(true);
        super.onDestroy();
    }

    public void createForeground(String title, String content, String cannel_name, int res_icon_in_drawable, Class<?> main) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel nc = new NotificationChannel("foreground_service", cannel_name == null ? "前台服务通知" : cannel_name, nm.IMPORTANCE_MIN);
            nm.createNotificationChannel(nc);
            nb = new Notification.Builder(this, "foreground_service");
        } else
            nb = new Notification.Builder(this);
        nb.setSmallIcon(res_icon_in_drawable);
        nb.setContentTitle(title);
        nb.setContentText(content);
        nb.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, main), PendingIntent.FLAG_UPDATE_CURRENT));
        startForeground(31, nb.build());
    }

}
