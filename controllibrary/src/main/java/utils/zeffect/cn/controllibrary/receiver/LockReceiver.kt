package utils.zeffect.cn.controllibrary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.startService
import utils.zeffect.cn.controllibrary.LockService

/**
 * 接收静态广播，开启服务。
 */
class LockReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.startService<LockService>()
    }
}