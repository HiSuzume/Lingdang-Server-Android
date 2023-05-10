package github.suzume.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @Author 铃芽
 * @Date 2023/04/29 15:39
 * @Describe 拉起服务Service
 */
public class WakeUpReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        ServerService.start(context,ServerService.class);
        
    }
    
}
