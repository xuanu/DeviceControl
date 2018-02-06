package utils.zeffect.cn.controllibrary.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import utils.zeffect.cn.controllibrary.R
import zeffect.cn.common.log.L


/**
 * 辅助安装或卸载应用
 *
 * 家长管控
 *
 * Created by Administrator on 2018/2/5.
 */
class AppAccessService : AccessibilityService() {
    private val installPkg = StringBuilder()
    private var nodeContents: List<String>? = null
    private var completeTexts: List<String>? = null

    companion object {
        const val ACTION_CHECK_TOP = "action.check.top.app"
        const val TOP_PACKAGE = "top_package"
        const val ACTION_ACTIVE_AUTO_INSTALL_APK = "action.active.auto.install.apk"
        const val ACTIVE_TIME = "active_time"
    }


    override fun onServiceConnected() {
        L.e("onServiceConnected")
        super.onServiceConnected()
        registerReceiver(mReciver, IntentFilter(ACTION_ACTIVE_AUTO_INSTALL_APK))
        //查找本机能够卸载应用的程序
        val i = Intent(Intent.ACTION_DELETE, Uri.parse(StringBuilder(32).append("package:")
                .append(packageName).toString()))
        packageManager.queryIntentActivities(i, PackageManager.MATCH_UNINSTALLED_PACKAGES).forEach {
            installPkg.append(it.activityInfo.packageName + ",")
        }
        //
        nodeContents = arrayListOf(resources.getString(R.string.auto_service_install)
                , resources.getString(R.string.auto_service_ensure)
                , resources.getString(R.string.auto_service_next)
                , resources.getString(R.string.auto_service_exchange)
                , resources.getString(R.string.auto_service_install__material_)
                , resources.getString(R.string.auto_service_ensure__material_)
                , resources.getString(R.string.auto_service_next__material_)
                , resources.getString(R.string.auto_service_exchange__material_))

        completeTexts = arrayListOf(resources.getString(R.string.auto_service_complete)
                , resources.getString(R.string.auto_service_complete__material_))
    }

    override fun unbindService(conn: ServiceConnection?) {
        super.unbindService(conn)
        unregisterReceiver(mReciver)
    }

    /**自动安装或卸载激活时间
     * 在这个时间的1分钟内的消息进行自动安装或卸载，省得别的地方调用安装也进行了安装。
     * **/
    private var mActiveTime: Long = 0L

    private val mReciver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action ?: ""
            if (action == ACTION_ACTIVE_AUTO_INSTALL_APK) {
                mActiveTime = p1?.getLongExtra(ACTIVE_TIME, 0L) ?: 0L
            }
        }
    }


    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        sendBroadcast(Intent(ACTION_CHECK_TOP).putExtra(TOP_PACKAGE, p0?.packageName))//发送顶层包名。
        //查找，当前是什么操作！
        if (installPkg.contains(p0?.packageName ?: "no package")) {
            val interval = System.currentTimeMillis() - mActiveTime
            if (interval < 60 * 1000) doUninstall(p0)//安装或卸载都可用。
        }


    }


    private fun doUninstall(p0: AccessibilityEvent?) {
        nodeContents?.forEach { findTextAndClick(it, p0) }
        completeTexts?.forEach { findTextAndClick(it, p0) }
    }

    /***
     * 查找文字并且点击
     */
    private fun findTextAndClick(text: String, p0: AccessibilityEvent?) {
        val nodes = arrayListOf<AccessibilityNodeInfo>()
        val rootNodeInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rootInActiveWindow
        } else {
            null
        }
        if (rootNodeInfo != null) {
            nodes.addAll(rootNodeInfo.findAccessibilityNodeInfosByText(text))
        }
        val nodeInfo = p0?.source
        if (nodeInfo != null) nodes.addAll(nodeInfo.findAccessibilityNodeInfosByText(text))
        nodes.forEach {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }


}