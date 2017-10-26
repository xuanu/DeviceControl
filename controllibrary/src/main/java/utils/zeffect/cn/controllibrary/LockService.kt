package utils.zeffect.cn.controllibrary

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.IBinder
import android.text.TextUtils
import utils.zeffect.cn.controllibrary.mvp.Constant
import utils.zeffect.cn.controllibrary.mvp.LockImp
import zeffect.cn.common.log.L


/**
 * Created by Administrator on 2017/10/14.
 */
class LockService : Service() {
    private val mLockImp by lazy { LockImp(this) }
    override fun onCreate() {
        super.onCreate()
        mLockImp.start(mLockImp.getUserId())
        reges()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        mLockImp.stop()
        startService(Intent(this, LockService::class.java))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action: String = intent?.getStringExtra(Constant.ACTION_KEY) ?: ""
        L.e("LockService receiver action:$action")
        when (action) {
            Constant.START_KEY -> {
                val userid: String = intent?.getStringExtra(Constant.USER_ID_KEY) ?: mLockImp.getUserId()
                if (TextUtils.isEmpty(userid)) mLockImp.start()
                else {
                    if (mLockImp.getUserId() != userid) mLockImp.changeUser(userid)
                    else mLockImp.start(userid)
                }
            }
            Constant.CHANGE_KEY -> {
                val userid: String = intent?.getStringExtra(Constant.USER_ID_KEY) ?: ""
                if (!TextUtils.isEmpty(userid)) mLockImp.changeUser(userid)
            }
        }
        return Service.START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun reges() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)//亮屏
        filter.addAction(Intent.ACTION_SCREEN_OFF)//关屏
//        filter.addAction(Intent.ACTION_TIME_TICK)//时间变化
        registerReceiver(receiver, filter)
        //
        val filter1 = IntentFilter()
        filter1.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter1.addAction(Intent.ACTION_BATTERY_LOW)
        filter1.addAction(LocationManager.MODE_CHANGED_ACTION)
        filter1.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter1.addDataScheme("package")
        registerReceiver(receiver, filter1)
        //
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent) {
            val action = p1?.action ?: ""
            L.e("receiver action $action")
            when (action) {
                Intent.ACTION_SCREEN_ON -> mLockImp.resume()
                Intent.ACTION_SCREEN_OFF -> mLockImp.pause()
                Intent.ACTION_TIME_TICK -> mLockImp.check()
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = p1.dataString.substring(8)
                    mLockImp.packageAdd(packageName)
                }
            }
        }

    }

}