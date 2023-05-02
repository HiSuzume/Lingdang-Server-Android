package github.suzume.server;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedDataManager {
    private Context c;
    private SharedPreferences sp;
    private final String tag = "suzume_server";

    public SharedDataManager(Context ct) {
        c = ct;
        sp = c.getSharedPreferences(tag, Context.MODE_PRIVATE);
    }

    public void set(String key, String value) {
        SharedPreferences.Editor spe=sp.edit();
        spe.putString(key, value);
        spe.commit();
    }

    public String get(String key, String def) {
        String s = sp.getString(key, def);
        if ("".equals(s))
            s = def;
        return s;
    }

    public String get(String k) {
        return get(k, null);
    }

}
