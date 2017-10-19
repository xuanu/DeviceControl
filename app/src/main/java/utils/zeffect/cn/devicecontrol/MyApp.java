package utils.zeffect.cn.devicecontrol;

import android.app.Application;
import android.content.Context;

import com.letv.sarrsdesktop.blockcanaryex.jrt.BlockCanaryEx;
import com.letv.sarrsdesktop.blockcanaryex.jrt.Config;
import com.squareup.leakcanary.LeakCanary;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/10/18
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 *         // TODO 用@see描述一下当前类的方法及简单解释
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 注册自定义的崩溃异常处理函数
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        boolean isInSamplerProcess = BlockCanaryEx.isInSamplerProcess(this);
        if(!isInSamplerProcess) {
            BlockCanaryEx.install(new Config(this));
        }
        if(!isInSamplerProcess) {
            //your code start here
        }
    }
}
