package utils.zeffect.cn.controllibrary.mvp

import android.app.Service
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.WindowManager
import utils.zeffect.cn.controllibrary.R
import utils.zeffect.cn.controllibrary.bean.App
import utils.zeffect.cn.controllibrary.bean.AppControl
import utils.zeffect.cn.controllibrary.bean.ControlUtils
import utils.zeffect.cn.controllibrary.utils.PackageUtils
import utils.zeffect.cn.controllibrary.utils.WeakHandler
import zeffect.cn.common.app.AppUtils
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

class LockImp(context: Context) {
    private val mLockView by lazy { LockView(context) }
    private val mContext by lazy { context }
    private var isStart = false
    private lateinit var mAppImp: AppControlImp
    private lateinit var mScreenImp: ScreenControlImp
    private lateinit var mDelImp: DelControlImp
    fun start(userid: String) {
        if (isStart) return
        mAppImp = AppControlImp(mContext, userid)
        mScreenImp = ScreenControlImp(mContext, userid)
        mDelImp = DelControlImp(mContext, userid)
        isStart = true
    }

    fun changeUser(newString: String) {
        mAppImp.changeUser(newString)
        mScreenImp.changeUser(newString)
        mDelImp.changeUser(newString)
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
    private val mView by lazy {
        LayoutInflater.from(context).inflate(R.layout.layout_control_screen, null).apply {
            //TODO 测试期间，点击移除
            this.setOnClickListener { remove() }
//            var mStart: Long = 0; this.setOnTouchListener { v, event ->
//            when (event.action) {
//                android.view.KeyEvent.ACTION_DOWN -> {
//                    mStart = System.currentTimeMillis()
//                }
//                android.view.KeyEvent.ACTION_UP -> {
//                    if ((System.currentTimeMillis()) - mStart > 60 * 1000) remove()
//                }
//            }
//            ;true
//        }
        }
    }

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
    val STATUS_OPEN = 1
    val WOB_BLACK = 0
    val WOB_WHITE = 1
    val CHANGE_KEY = "change"
}


class AppControlImp(context: Context, userid: String) : MyFileObserver.FileListener {
    override fun change(path: String) {
        changeControl(path)
    }

    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
    private val FILE_NAME = ".appcontrol"
    private val mSingleThreadExecutor = Executors.newSingleThreadExecutor()
    private var mAppRun: AppRunable? = null
    private var mUserId = userid

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        val path = startWatch(userid)
        startControl(ControlUtils.json2App(ControlUtils.readFile(path)))
    }

    fun changeUser(newUserid: String) {
        if (mUserId == newUserid) return
        mUserId = newUserid
        stopWathch()
        startWatch(newUserid)
    }

    fun stop() {
        stopWathch()
        mAppRun?.setRuning(false)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}$FILE_NAME"
        val tempFile = File(path)
        if (!tempFile.exists()) {
            tempFile.parentFile.mkdirs()
            tempFile.createNewFile()
        }
        mFileObserver = MyFileObserver(path).apply { setFileListener(this@AppControlImp) }
        mFileObserver?.startWatching()
        return path
    }

    private fun stopWathch() {
        mFileObserver?.stopWatching()
    }

    private fun startControl(mAppContrl: AppControl) {
        if (mAppContrl == null) return
        if (mAppContrl?.status != Constant.STATUS_OPEN) return
        if (mAppRun == null || !mAppRun?.getRun()!!) mAppRun = AppRunable(mContext, mAppContrl)
        mSingleThreadExecutor.execute(mAppRun)
    }

    private fun changeControl(path: String) {
        val tempContrl = ControlUtils.json2App(ControlUtils.readFile(path))
        if (!mAppRun?.getRun()!!) startControl(tempContrl)
        mAppRun?.setControl(tempContrl)
    }


    class AppRunable(context: Context, control: AppControl) : Runnable {
        private var mControl = control
        private val mContext = context
        private var isRun = true
        private var isPause = false
        private val mSystemApp by lazy {
            val tempSytemApp: StringBuilder = StringBuilder()
            AppUtils.getApps(context, 1).forEach { tempSytemApp.append("${it.packageName},") }
            tempSytemApp
        }

        override fun run() {
            if (mControl == null) {
                stop()
            }
            if (mControl.status != Constant.STATUS_OPEN) {
                stop()
            }
            val packages = mControl.apps
            if (TextUtils.isEmpty(packages)) {
                stop()
            }
            while (isRun) {
                Thread.sleep(5000)
                if (isPause) continue
                val wob = mControl.wob
                val topApp = AppUtils.getTopPackageName(mContext)
                if (TextUtils.isEmpty(topApp)) {
                    continue
                }
                if (mSystemApp.contains(topApp)) continue
                when (wob) {
                    Constant.WOB_WHITE -> {
                        if (!packages.contains(topApp)) ControlUtils.goHome(mContext)//不在白名单
                    }
                    else -> {
                        if (packages.contains(topApp)) ControlUtils.goHome(mContext)//在黑名单
                    }
                }
            }
        }

        fun setRuning(pisRun: Boolean) {
            this.isRun = pisRun
        }

        fun getRun(): Boolean {
            return this.isRun
        }

        fun setControl(control: AppControl) {
            this.mControl = control
        }

        private fun stop() {
            isRun = false
        }
    }
}

class ScreenControlImp(context: Context, userid: String) : MyFileObserver.FileListener {
    override fun change(path: String) {
        check(path)
    }

    private var mUserId = userid
    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
    private val FILE_NAME = ".screencontrol"
    private val mLockView by lazy { LockView(context) }

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        check(startWatch(userid))
    }

    fun check(path: String) {
        val control = ControlUtils.json2Screen(ControlUtils.readFile(path))
        if (control == null) {
            mLockView.remove()
            return
        }
        if (control.status != Constant.STATUS_OPEN) {
            mLockView.remove()
            return
        }
        if (control.start != 0 && control.end != 0) {

        } else {
            mLockView.show()
        }
    }

    fun changeUser(newUserid: String) {
        if (mUserId == newUserid) return
        mUserId = newUserid
        stop()
        start(newUserid)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}$FILE_NAME"
        val tempFile = File(path)
        if (!tempFile.exists()) {
            tempFile.parentFile.mkdirs()
            tempFile.createNewFile()
        }
        mFileObserver = MyFileObserver(path).apply { setFileListener(this@ScreenControlImp) }
        mFileObserver?.startWatching()
        return path
    }

    private fun stopWathch() {
        mFileObserver?.stopWatching()
    }

    fun stop() {
        stopWathch()
        mLockView.remove()
    }
}


class DelControlImp(context: Context, userid: String) : MyFileObserver.FileListener {
    override fun change(path: String) {
        check(mUserid, path)
    }

    private var mUserid = userid
    private val mContext = context
    private val mTask = arrayListOf<DelRun>()
    private val mSemp = Semaphore(1)
    private val mSingle by lazy { Executors.newSingleThreadExecutor() }
    private var mRunDelRun: DelRun? = null
    private val mHandler by lazy {
        WeakHandler(context.mainLooper, Handler.Callback { message ->
            if (message.what == 0x99) {
                if (mTask.isNotEmpty()) {
                    mSemp.acquire()
                    mSingle.execute(mRunDelRun.run { mTask.removeAt(0) })
                }
            }
            true
        })
    }
    private var mFileObserver: MyFileObserver? = null
    private val FILE_NAME = ".delcontrol"

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        val path = startWatch(userid)
        check(userid, path)
    }

    /***
     * 这个不对外开放，因为不需要外部来检查
     */
    private fun check(userid: String, path: String) {
        if (TextUtils.isEmpty(userid)) return
        if (TextUtils.isEmpty(path)) return
        val contrl = ControlUtils.json2Del(ControlUtils.readFile(path))
        if (contrl.status != Constant.STATUS_OPEN) return
        val tempList = contrl.apps
        if (tempList.isEmpty()) return
        mTask.filter { it.getControl().enable != Constant.STATUS_OPEN }
        tempList.forEach {
            mTask.add(DelRun(mContext, userid, it, mSemp, mHandler))
        }

    }

    fun changeUser(newUserid: String) {
        if (newUserid == mUserid) return
        mUserid = newUserid
        stop()
        start(newUserid)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}$FILE_NAME"
        val tempFile = File(path)
        if (!tempFile.exists()) {
            tempFile.parentFile.mkdirs()
            tempFile.createNewFile()
        }
        mFileObserver = MyFileObserver(path).apply { setFileListener(this@DelControlImp) }
        mFileObserver?.startWatching()
        return path
    }

    private fun stopWathch() {
        mFileObserver?.stopWatching()
    }

    fun stop() {
        stopWathch()
        mRunDelRun?.setExit(true)
        mTask.clear()
    }


    class DelRun(context: Context, userid: String, delApp: App, semaphore: Semaphore, handler: WeakHandler) : Runnable {
        private val mContext = context
        private val mUserid = userid
        private val mDelApp = delApp
        private val mHandler = handler
        private val mSemap = semaphore
        private val INSTALLER_PACK = "com.android.packageinstaller"
        private var isPause = false
        private var isRun = false
        private val mSystemApp by lazy {
            val tempSys = StringBuilder()
            AppUtils.getApps(mContext, 1).forEach { tempSys.append(it.packageName + ",") }
            tempSys
        }

        override fun run() {
            if (TextUtils.isEmpty(mDelApp.packagename)) {
                stop()
                return
            }
            if (mSystemApp.contains(mDelApp.packagename)) {
                stop()
                return
            }
            val lastUserId = mUserid
            while (isRun) {
                if (lastUserId != mUserid) {
                    stop();break
                }
                Thread.sleep(5000)
                if (isPause) {
                    continue
                }
                val isExist = AppUtils.isPackageExist(mContext, mDelApp.packagename)
                if (!isExist) {
                    stop()
                    mHandler.sendEmptyMessage(0x99)
                    break
                }
                val topApp = AppUtils.getTopPackageName(mContext)
                if (TextUtils.isEmpty(topApp)) continue
                if (topApp == INSTALLER_PACK) continue
                PackageUtils.uninstallNormal(mContext, mDelApp.packagename)
            }
            stop()
        }


        fun setExit(pisRun: Boolean) {
            this.isRun = pisRun
        }

        private fun stop() {
            mSemap.release()
        }

        fun getControl(): App {
            return mDelApp
        }

    }


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