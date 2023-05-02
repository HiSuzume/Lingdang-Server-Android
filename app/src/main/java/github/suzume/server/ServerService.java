package github.suzume.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import java.io.IOException;

/**
 * @Author 铃芽
 * @Date 2023/04/29 15:41
 * @Describe 服务开启
 */
public class ServerService extends Service {
    public static boolean isstart = false;
    private HttpServer server;
    private static ServerService self;
    private SharedDataManager sd;

    public static void start(Context c) {
        if (!isstart) {
            if (Build.VERSION.SDK_INT >= 26)
                c.startForegroundService(new Intent(c, ServerService.class));
            c.startService(new Intent(c, ServerService.class));
        }
    }

    public static void stop() {
        if (isstart && self != null)
            self.stopSelf();
    }

    @Override
    public void onCreate() {
        self = this;
        sd = new SharedDataManager(this);
        super.onCreate();
        try {
            server = new HttpServer(Integer.parseInt(sd.get("http_port", "8080")), sd.get("http_dir", "/sdcard"));
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isstart) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            Notification.Builder nb;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel nc = new NotificationChannel("foreground", "foreground", nm.IMPORTANCE_MIN);
                nm.createNotificationChannel(nc);
                nb = new Notification.Builder(this, "foreground");
            } else
                nb = new Notification.Builder(this);
            nb.setSmallIcon(R.drawable.ic_launcher);
            nb.setContentTitle("Suzume Server");
            nb.setContentText("我出发啦!");
            nb.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
            startForeground(31, nb.build());
        }
        isstart = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        isstart = false;
        stopForeground(true);
        server.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
