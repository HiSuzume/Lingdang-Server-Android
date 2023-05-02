package github.suzume.server;
import android.app.Application;

/**
 * @Author 铃芽
 * @Date 2023/04/30 21:33
 * @备注 崩溃信息收集
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Crash.getInstance().init(this);
    }
    
}
