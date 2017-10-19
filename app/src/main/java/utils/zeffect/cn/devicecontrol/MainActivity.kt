package utils.zeffect.cn.devicecontrol

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import utils.zeffect.cn.controllibrary.bean.ControlUtils
import utils.zeffect.cn.controllibrary.utils.app.UsageStatsUtils
import zeffect.cn.common.app.AppUtils


class MainActivity : Activity() {
    private val USERID = "123456789"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startAll.setOnClickListener {
                        startAll()
//            toast(AppUtils.getTopPackageName(this, 5))
        }
        startWhiteApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(1, 1)) }
        startBlackApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(1, 0)) }
        closeApp.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.App(0)) }
        startScreen.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.ScreenControl(1)) }
        closeScreen.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.ScreenControl(0)) }
        startDel.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.DekControl(1)) }
        closeDel.setOnClickListener { ControlUtils.updateControl(USERID, SimulData.DekControl(0)) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) startAll()
    }

    private fun startAll() {
        if (UsageStatsUtils.hasUsageOption(this) && !UsageStatsUtils.isOpen(this)) {
            UsageStatsUtils.openUsageSetting(this, 100)
        } else if (!checkSettingAlertPermission(this, 100)) {
        } else {
            ControlUtils.start(this, USERID)
        }
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
