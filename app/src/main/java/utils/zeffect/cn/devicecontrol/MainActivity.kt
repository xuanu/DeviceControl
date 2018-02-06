package utils.zeffect.cn.devicecontrol

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import utils.zeffect.cn.controllibrary.accessibility.AppAccessService
import utils.zeffect.cn.controllibrary.bean.ControlUtils
import utils.zeffect.cn.controllibrary.utils.PackageUtils
import zeffect.cn.common.log.L
import java.io.File


class MainActivity : Activity() {
    private val USERID = "123456789"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        L.isDebug = BuildConfig.DEBUG
        startAll.setOnClickListener {
            startAll()
        }
        startWhiteApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(1, 1)) }
        startBlackApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(1, 0)) }
        closeApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(0)) }
        startScreen.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.ScreenControl(1)) }
        closeScreen.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.ScreenControl(0)) }
        startDel.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.DekControl(1)) }
        closeDel.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.DekControl(0)) }
        touchTest.setOnClickListener { Toast.makeText(this, "点击测试，屏幕有无响应！", Toast.LENGTH_SHORT).show() }
        testUninstall.setOnClickListener {
            val i = Intent(Intent.ACTION_DELETE, Uri.parse(StringBuilder(32).append("package:")
                    .append(packageName).toString()))
            packageManager.queryIntentActivities(i, PackageManager.MATCH_UNINSTALLED_PACKAGES).forEach {
                L.e("输出信息：${it.activityInfo.packageName},${it.activityInfo.targetActivity},${it?.toString()}")
            }
        }
        testInstall.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.setDataAndType(Uri.EMPTY, "application/vnd.android.package-archive")
            packageManager.queryIntentActivities(i, PackageManager.MATCH_UNINSTALLED_PACKAGES).forEach {
                L.e("输出信息：${it.activityInfo.packageName},${it.activityInfo.targetActivity},${it?.toString()}")
            }
        }
        installBdc.setOnClickListener {
            //            sendBroadcast(Intent(AppAccessService.ACTION_ACTIVE_AUTO_INSTALL_APK).putExtra(AppAccessService.ACTIVE_TIME, System.currentTimeMillis()))
//            PackageUtils.installNormal(this, File("${Environment.getExternalStorageDirectory().absolutePath}${File.separator}bdc.apk").absolutePath)
            ControlUtils.goAccess(this)
        }

        uninstallBdc.setOnClickListener {
            Toast.makeText(this, "有无权限:" + ControlUtils.checkAccessibilityEnabled(this), Toast.LENGTH_SHORT).show()
            sendBroadcast(Intent(AppAccessService.ACTION_ACTIVE_AUTO_INSTALL_APK).putExtra(AppAccessService.ACTIVE_TIME, System.currentTimeMillis()))
//            PackageUtils.uninstallNormal(this, "com.ozing.bdc.activity")
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) startAll()
    }

    private fun startAll() {
        ControlUtils.start(this, USERID)
    }

    /**
     * 检测系统弹出权限
     *
     * @param cxt 上下文
     * @param req 返回码
     * @return 有无权限
     */
    fun checkSettingAlertPermission(cxt: Any, req: Int): Boolean {
        if (cxt is Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(cxt.baseContext)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + cxt.packageName))
                    cxt.startActivityForResult(intent, req)
                    return false
                }
            }
        } else if (cxt is Fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(cxt.activity)) {

                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + cxt.activity.packageName))
                    cxt.startActivityForResult(intent, req)
                    return false
                }
            }
        } else {
            throw RuntimeException("cxt is net a activity or fragment")
        }
        return true
    }


}
