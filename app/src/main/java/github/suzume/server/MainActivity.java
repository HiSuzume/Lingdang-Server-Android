package github.suzume.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.widget.EditText;
import android.preference.PreferenceActivity;
import android.os.Handler;
import android.app.ActivityManager;

public class MainActivity extends Activity {
    private SharedDataManager sd;

    public Dialog circleDialog(Dialog d) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(0xFF424242);
        float radiu = 40;
        gd.setCornerRadii(new float[]{radiu,radiu,radiu,radiu,radiu,radiu,radiu,radiu});
        d.getWindow().setBackgroundDrawable(gd);
        return d;
    }

    public void toast(CharSequence c) {
        Toast.makeText(this, c, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        sd = new SharedDataManager(this);

        //setTheme(android.R.style.Theme_DeviceDefault_Wallpaper_NoTitleBar);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                                   android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                               }, 0);
        }

        //Toast.makeText(this,"服务启动中...",Toast.LENGTH_SHORT).show();

        //隐藏后台
        getSystemService(ActivityManager.class).getAppTasks().get(0).setExcludeFromRecents(true);

        showMe();
    }

    //有点难看，请各位大神轻喷（
    private void showMe() {

        final SettingsUtil.OnExitCallback ecb = new SettingsUtil.OnExitCallback(){
            @Override
            public void onExit() {
                showMe();
            }
        };

        final SettingsUtil.Setting root_httpconfig_port = new SettingsUtil.Setting("http_port", "Http端口", SettingsUtil.Setting.Type.EDIT, 
            new SettingsUtil.Setting.Callback(){
                public void onCallback(SettingsUtil s) {}
                public void onCallback(SettingsUtil s, boolean b) {}
                @Override
                public void onCallback(SettingsUtil s, String c) {
                    s.set("http_port", c);
                    toast("记得让铃芽回来哦~");
                }
            });

        root_httpconfig_port.data = "默认8080，80在安卓无法使用";

        final SettingsUtil.Setting root_httpconfig_dir = new SettingsUtil.Setting("http_dir", "Http文件路径", SettingsUtil.Setting.Type.EDIT, 
            new SettingsUtil.Setting.Callback(){
                public void onCallback(SettingsUtil s) {}
                public void onCallback(SettingsUtil s, boolean b) {}
                @Override
                public void onCallback(SettingsUtil s, String c) {
                    s.set("http_dir", c);
                    toast("记得让铃芽回来哦~");
                }
            });

        root_httpconfig_dir.data = "不以\"/\"结尾的文件夹路径";

        final SettingsUtil.Setting root_httpconfig = new SettingsUtil.Setting("", "Http配置", SettingsUtil.Setting.Type.CHOICE, 
            new SettingsUtil.Setting.Callback(){
                public void onCallback(SettingsUtil s) {}
                public void onCallback(SettingsUtil s, boolean b) {}
                @Override
                public void onCallback(SettingsUtil s, String c) {
                    switch (c) {
                        case "端口":
                            s.openSettingsDialog(root_httpconfig_port, ecb);
                            break;
                        case "文件路径":
                            s.openSettingsDialog(root_httpconfig_dir, ecb);
                            break;
                    }
                }
            });

        root_httpconfig_port.parent = root_httpconfig;
        root_httpconfig_dir.parent = root_httpconfig;

        root_httpconfig.data = new String[]{
            "端口",
            "文件路径",
        };

        final SettingsUtil.Setting root = new SettingsUtil.Setting("", "面板", SettingsUtil.Setting.Type.CHOICE, 
            new SettingsUtil.Setting.Callback(){
                public void onCallback(SettingsUtil s) {}
                public void onCallback(SettingsUtil s, boolean boolean_) {}
                @Override
                public void onCallback(SettingsUtil s, String c) {
                    switch (c) {
                        case "Http配置":
                            s.openSettingsDialog(root_httpconfig, ecb);
                            break;
                    }
                }
            });

        root_httpconfig.parent = root;

        root.data = new String[]{
            "Http配置",
        };

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("面板")
            .setMessage("若通知栏显示了通知，那么铃芽已经出发啦~\n\n点击空白处退出面板!")
            .setOnCancelListener(new AlertDialog.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            })
            .setPositiveButton("去吧", new AlertDialog.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    toast("我出发啦!");
                    sd.set("开关", "开");
                    ServerService.start(MainActivity.this);
                    showMe();
                }
            })
            .setNegativeButton("来吧", new AlertDialog.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    toast("我回来啦!");
                    sd.set("开关", "关");
                    ServerService.stop();
                    showMe();
                }
            })
            .setNeutralButton("设置", new AlertDialog.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new SettingsUtil(MainActivity.this).openSettingsDialog(root, ecb);
                }
            });
        circleDialog(b.create()).show();
    }

    /*private void showMe() {
     //由于对 PreferenceActivity 不了解，因此使用老一套——多AlertDialog层叠
     AlertDialog.Builder b = new AlertDialog.Builder(this);
     b.setTitle("面板")
     .setMessage("若通知栏显示了通知，那么铃芽已经出发啦~\n\n点击空白处退出面板!")
     .setOnCancelListener(new AlertDialog.OnCancelListener(){
     @Override
     public void onCancel(DialogInterface dialog) {
     finish();
     }
     })
     .setPositiveButton("去吧", new AlertDialog.OnClickListener(){
     @Override
     public void onClick(DialogInterface dialog, int which) {
     toast("我出发啦!");
     sd.set("开关", "开");
     ServerService.start(MainActivity.this);
     showMe();
     }
     })
     .setNegativeButton("来吧", new AlertDialog.OnClickListener(){
     @Override
     public void onClick(DialogInterface dialog, int which) {
     toast("我回来啦!");
     sd.set("开关", "关");
     ServerService.stop();
     showMe();
     }
     })
     .setNeutralButton("设置", new AlertDialog.OnClickListener(){
     @Override
     public void onClick(DialogInterface dialog, int which) {
     final String[] sl = new String[]{
     "HTTP端口",
     "HTTP路径",
     };
     AlertDialog.Builder dlb =new AlertDialog.Builder(MainActivity.this)
     .setTitle("设置")
     .setCancelable(false)
     .setNegativeButton("取消", new DialogInterface.OnClickListener(){
     @Override
     public void onClick(DialogInterface dialog, int which) {
     showMe();
     }
     })
     .setItems(sl, new DialogInterface.OnClickListener(){
     @Override
     public void onClick(DialogInterface d, int w) {
     switch (sl[w]) {
     case "HTTP端口":
     final EditText hpet = new EditText(MainActivity.this);
     hpet.setHint("Android默认占用80端口，不设置则为8080");
     hpet.setText(sd.get("http_port", ""));
     AlertDialog.Builder dlb = new AlertDialog.Builder(MainActivity.this)
     .setTitle("设置端口")
     .setView(hpet)
     .setCancelable(false)
     .setPositiveButton("保存", new DialogInterface.OnClickListener(){
     @Override
     public void onClick(DialogInterface d, int w) {
     sd.set("http_port", hpet.getText().toString());
     toast("记得让铃芽回来哦~");
     showMe();
     }
     });
     Dialog dl = dlb.create();
     dl = circleDialog(dl);
     dl.show();
     break;
     case "HTTP路径":
     final EditText hdet = new EditText(MainActivity.this);
     hdet.setHint("如 /sdcard 请勿斜杠结尾");
     hdet.setText(sd.get("http_dir", ""));
     AlertDialog.Builder dlb2 = new AlertDialog.Builder(MainActivity.this)
     .setTitle("设置文件路径")
     .setView(hdet)
     .setCancelable(false)
     .setPositiveButton("保存", new DialogInterface.OnClickListener(){
     @Override
     public void onClick(DialogInterface d, int w) {
     sd.set("http_dir", hdet.getText().toString());
     toast("记得让铃芽回来哦~");
     showMe();
     }
     });
     Dialog dl2 = dlb2.create();
     dl2 = circleDialog(dl2);
     dl2.show();
     break;
     }
     }
     });
     Dialog dl = dlb.create();
     dl = circleDialog(dl);
     dl.show();
     }
     });
     Dialog d = b.create();
     d = circleDialog(d);
     d.show();
     }*/

}
