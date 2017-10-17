package utils.zeffect.cn.controllibrary.utils.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

/**
 * UsageStatsManger使用类，用于捕获最近任务
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/06/20
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class UsageStatsUtils {
    /***
     * 有没有这个设置，必须要开启这个设置才能得到最近任务
     * @param pContext 上下文
     * @return true有false没有
     */
    public static boolean hasUsageOption(Context pContext) {
        PackageManager packageManager = pContext.getApplicationContext()
                .getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /***
     * 当前的状态
     * @param pContext 上下文
     * @return true开启false关闭
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isOpen(Context pContext) {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) pContext.getApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return false;
        }
        return true;
    }

    /***
     * 打开权限界面
     * @param pContext 上下文
     * @param requestCode 请求码
     */
    public static void openUsageSetting(Context pContext, int requestCode) {
        if (pContext == null) {
            throw new RuntimeException(UsageStatsUtils.class.getName() + " context null");
        }
        Intent intent = new Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS);
        if (pContext instanceof Activity) {
            ((Activity) pContext).startActivityForResult(intent, requestCode);
        }
    }

}
