package utils.zeffect.cn.controllibrary.mvp

import android.app.Service
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import utils.zeffect.cn.controllibrary.R
import utils.zeffect.cn.controllibrary.bean.AppControl

class LockImp(context: Context) {
    private val mLockView by lazy { LockView(context) }
    private var isStart = false
    private lateinit var mAppImp: AppControlImp
    private lateinit var mScreenImp: ScreenControlImp
    private lateinit var mDelImp: DelControlImp
    fun start(userid: String) {
        if (isStart) return
        mAppImp = AppControlImp(userid)
        mAppImp.start()
        mScreenImp = ScreenControlImp(userid)
        mScreenImp.start()
        mDelImp = DelControlImp(userid)
        mDelImp.start()
        isStart = true
    }

    fun stop() {
        mAppImp.stop()
        mScreenImp.stop()
        mDelImp.stop()
        isStart = false
    }
}


class LockView(context: Context) {
    private val mWm by lazy { context.getSystemService(Service.WINDOW_SERVICE) as WindowManager }
    private val mParam by lazy {
        WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT).apply {
            this.alpha = 0f
            this.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            this.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
    }
    private val mView by lazy { LayoutInflater.from(context).inflate(R.layout.layout_control_screen, null).apply { this.setOnTouchListener { v, event -> ;true } } }

    /**是否添加了布局，这个是锁屏布局**/
    private var isAdd = false


    fun show() {
        if (isAdd) return
        mWm.addView(mView, mParam)
        isAdd = true
    }

    fun remove() {
        if (!isAdd) return
        mWm.removeViewImmediate(mView)
    }

}


object Constant {
    val ACTION_KEY = "action"
    val START_KEY = "start"
    val USER_ID_KEY = "userid"
}


class AppControlImp(userid: String) {
    private lateinit var mAppContrl: AppControl
    fun start() {
        if (mAppContrl != null) return
    }

    fun changeUser() {}
    fun stop() {}
}

class ScreenControlImp(userid: String) {
    fun start() {}
    fun changeUser() {}
    fun stop() {}
}


class DelControlImp(userid: String) {
    fun start() {}
    fun changeUser() {}
    fun stop() {}
}