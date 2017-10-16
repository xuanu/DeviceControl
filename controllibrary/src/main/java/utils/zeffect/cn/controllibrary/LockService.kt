package utils.zeffect.cn.controllibrary

import android.app.Service
import android.content.Intent
import android.os.IBinder
import utils.zeffect.cn.controllibrary.mvp.Constant
import utils.zeffect.cn.controllibrary.mvp.LockImp

/**
 * Created by Administrator on 2017/10/14.
 */
class LockService : Service() {
    private val mLockImp by lazy { LockImp(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action: String = intent?.getStringExtra(Constant.ACTION_KEY) ?: ""
        when (action) {
            Constant.START_KEY -> {
                val userid: String = intent?.getStringExtra(Constant.USER_ID_KEY) ?: ""
                mLockImp.start(userid)
            }
        }
        return Service.START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}