package zeffect.cn.common.app

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.text.TextUtils


/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/09/28
 *      desc    ：
 *      version:：1.0
 * </pre>
 * @author zzx
 */
object AppUtils {
    /**
     * 根据包名判断应用是否已经安装。
     *
     * @param context     上下文
     * @param packageName 包名
     * @return 如果应用已经安装，则返回true，否则返回false.
     */
    fun isPackageExist(context: Context, packageName: String): Boolean {
        if (TextUtils.isEmpty(packageName)) return false
        var isExist = false
        try {
            isExist = null != context.packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES) && null != context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
        } finally {
            return isExist
        }
    }

    /***
     * 获取本机app，
     * @param context 上下文
     * @param type 类型，0用户安装的，1系统应用，2全部应用
     */
    fun getApps(context: Context, type: Int = 0): List<PackageInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES or PackageManager.GET_PERMISSIONS)
        val retuApps = arrayListOf<PackageInfo>()
        apps.forEach {
            val appInfo = it.applicationInfo
            when (type) {
                0 -> {
                    if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        retuApps.add(it)
                    }
                }
                1 -> {
                    if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) > 0) {
                        retuApps.add(it)
                    }
                }
                2 -> {
                    retuApps.add(it)
                }
            }
        }
        return retuApps
    }


    /***
     * 获取所有带启动启动界面的
     */
    fun getHasMainInfo(context: Context): List<ResolveInfo> {
        val tempManager = context.getPackageManager()
        val tempIntent = Intent()
        tempIntent.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return tempManager.queryIntentActivities(tempIntent, 0)
    }

    /**
     * 得到栈顶的应用包
     *
     * @param context 上下文
     * @param time 最近几秒的应用情况,单位秒
     * @return
     */
    fun getTopPackageName(context: Context, defaultTime: Int = 5): String {
        var retuString = ""
        if (context == null) {
            return retuString
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val usm = context!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val event = UsageEvents.Event()
                val usageEvents = usm.queryEvents(time - 5 * 1000, time)
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        retuString = event.packageName
                    }
                }
//                val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - defaultTime * 1000, time)//检查5秒内的应用情况
//                if (appList != null && appList.size > 0) {
//                    val mySortedMap = TreeMap<Long, UsageStats>()
//                    for (usageStats in appList) {
//                        mySortedMap.put(usageStats.lastTimeUsed, usageStats)
//                    }
//                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                        retuString = mySortedMap[mySortedMap.lastKey()]?.packageName ?: ""
//                    }
//                }
            } else {
                val activityManager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasksInfo = activityManager.getRunningTasks(1)
                if (tasksInfo.isNotEmpty()) {
                    retuString = tasksInfo[0].topActivity.packageName
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return retuString
        }
    }
}
