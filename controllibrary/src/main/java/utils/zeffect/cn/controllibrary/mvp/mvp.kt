package utils.zeffect.cn.controllibrary.mvp

import android.app.Service
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.WindowManager
import utils.zeffect.cn.controllibrary.R
import utils.zeffect.cn.controllibrary.bean.AppControl
import java.io.File

class LockImp(context: Context) {
    private val mLockView by lazy { LockView(context) }
    private var isStart = false
    private lateinit var mAppImp: AppControlImp
    private lateinit var mScreenImp: ScreenControlImp
    private lateinit var mDelImp: DelControlImp
    fun start(userid: String) {
        if (isStart) return
        mAppImp = AppControlImp()
        mAppImp.start(userid)
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
    val SD_PATH = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}Device${File.separator}"
}


class AppControlImp() : MyFileObserver.FileListener {
    override fun change(path: String) {

    }

    private lateinit var mAppContrl: AppControl
    private var mFileObserver: MyFileObserver? = null
    private var mUserId: String = ""
    private val FILE_NAME = ".appcontrol"
    fun start(userid: String) {
        if (userid == mUserId) return
        stop()
        val path = "${Constant.SD_PATH}$userid${File.separator}$FILE_NAME"
        mFileObserver = MyFileObserver(path)
        mFileObserver?.startWatching()

    }

    fun check() {}
    fun changeUser() {}
    fun stop() {
        mFileObserver?.stopWatching()
    }
}

class ScreenControlImp(userid: String) {
    fun start() {}
    fun check() {}
    fun changeUser() {}
    fun stop() {}
}


class DelControlImp(userid: String) {
    fun start() {}
    fun check() {}
    fun changeUser() {}
    fun stop() {}
}

class MyFileObserver(path: String) : FileObserver(path) {
    private var mFileListener: FileListener? = null
    private val mPath by lazy { path }
    fun setFileListener(listener: FileListener) {
        this.mFileListener = listener
    }

    override fun onEvent(event: Int, path: String?) {
        when (event and FileObserver.ALL_EVENTS) {
            FileObserver.MODIFY -> mFileListener?.change(mPath)
            FileObserver.CREATE -> mFileListener?.change(mPath)
        }
    }


    interface FileListener {
        fun change(path: String)
    }
}