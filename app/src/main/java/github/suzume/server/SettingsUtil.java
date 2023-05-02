package github.suzume.server;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author 铃芽
 * @Date 2023/05/02 11:55
 * @Describe 封装设置，一言难尽
 */
public class SettingsUtil {
    private Context mContext;
    private SharedDataManager sdm;

    public static class Setting {

        public static class Type {
            public static final int EDIT = 0;
            public static final int CLICK = 1;
            public static final int CHOICE = 2;
            public static final int SWITCH = 3;
        }

        public interface Callback {
            /*
             * EDIT:SettingsUtil, 编辑内容(字符串)
             * CLICK:SettingsUtil
             * CHOICE:SettingsUtil, 选中内容(字符串)
             * SWITCH:SettingsUtil, 状态(布尔)
             */
            public void onCallback(SettingsUtil s);
            public void onCallback(SettingsUtil s, boolean boolean_);
            public void onCallback(SettingsUtil s, String choice_or_edit);
        }

        public final int type;
        public final String name;
        //对话框标题
        public final String title;
        private final Callback callback;
        /*
         * EDIT:备注
         * CLICK:无
         * CHOICE:选项(String[])
         * SWITCH:备注
         */
        public Object data;
        //private final Map<String,Setting> sub;
        public Setting parent;

        public Setting(String name_, String title_, int type_, Callback cb) {
            name = name_;
            type = type_;
            title = title_;
            callback = cb;
            //sub = new HashMap<String,Setting>();
        }

        /*public void setSub(String name, Setting s) {
         sub.put(name, s);
         }

         public Setting getSub(String name) {
         return sub.get(name);
         }*/

        public void call(SettingsUtil s) {
            if (callback != null)
                callback.onCallback(s);
        }

        public void call(SettingsUtil s, String s1) {
            if (callback != null)
                callback.onCallback(s, s1);
        }

        public void call(SettingsUtil s, boolean b) {
            if (callback != null)
                callback.onCallback(s, b);
        }

    }

    public SettingsUtil(Context c) {
        mContext = c;
        sdm = new SharedDataManager(mContext);
    }

    public Dialog circleDialog(Dialog d) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(0xFF424242);
        float radiu = 40;
        gd.setCornerRadii(new float[]{radiu,radiu,radiu,radiu,radiu,radiu,radiu,radiu});
        d.getWindow().setBackgroundDrawable(gd);
        return d;
    }

    public interface OnExitCallback {
        public void onExit();
    }

    public void openSettingsDialog(final Setting s, final OnExitCallback cb) {
        AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
        switch (s.type) {
            case Setting.Type.CLICK:
                s.call(SettingsUtil.this);
                Setting p = s.parent;
                if (p != null)
                    openSettingsDialog(p, cb);
                else
                    cb.onExit();
                break;
            case Setting.Type.CHOICE:
                final String[] items = (String[]) s.data;
                ab.setTitle(s.title)
                    .setCancelable(false)
                    .setItems(items, new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            s.call(SettingsUtil.this, items[which]);
                        }
                    })
                    .setNegativeButton("关闭", new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    });
                circleDialog(ab.create()).show();
                break;
            case Setting.Type.EDIT:
                final EditText hpet = new EditText(mContext);
                hpet.setHint((String) s.data);
                hpet.setText(sdm.get(s.name, ""));
                ab.setTitle(s.title)
                    .setView(hpet)
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface d, int w) {
                            s.call(SettingsUtil.this, hpet.getText().toString());
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    })
                    .setNegativeButton("取消", new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    });
                circleDialog(ab.create()).show();
                break;
            case Setting.Type.SWITCH:
                ab.setTitle(s.title)
                    .setMessage((String) s.data)
                    .setCancelable(false)
                    .setPositiveButton("去吧", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface d, int w) {
                            s.call(SettingsUtil.this, true);
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    })
                    .setNegativeButton("回来吧", new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            s.call(SettingsUtil.this, false);
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    })
                    .setNeutralButton("取消", new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Setting p = s.parent;
                            if (p != null)
                                openSettingsDialog(p, cb);
                            else
                                cb.onExit();
                        }
                    });
                circleDialog(ab.create()).show();
                break;
        }
    }

    public String get(String key) {
        return sdm.get(key);
    }

    public void set(String key, String v) {
        sdm.set(key, v);
    }

}
