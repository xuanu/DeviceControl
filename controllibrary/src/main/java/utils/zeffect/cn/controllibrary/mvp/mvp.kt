package utils.zeffect.cn.controllibrary.mvp

import android.app.Service
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.WindowManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import utils.zeffect.cn.controllibrary.R
import utils.zeffect.cn.controllibrary.bean.*
import utils.zeffect.cn.controllibrary.utils.PackageUtils
import utils.zeffect.cn.controllibrary.utils.WeakHandler
import zeffect.cn.common.app.AppUtils
import zeffect.cn.common.sp.SpUtils
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

interface ControlInterface {
    fun changeUser(newUserid: String)
    fun check()
    fun pause()
    fun resume()
    fun stop()
}

class LockImp(context: Context) : ControlInterface {
    private val mContext by lazy { context }
    private var isStart = false
    private var mAppImp: AppControlImp? = null
    private var mScreenImp: ScreenControlImp? = null
    private var mDelImp: DelControlImp? = null
    private var isPause = false
    fun start(userid: String = Constant.DEFAUTL_USER_ID) {
        if (isStart) return
        isPause = false
        mAppImp = AppControlImp(mContext, userid)
        mScreenImp = ScreenControlImp(mContext, userid)
        mDelImp = DelControlImp(mContext, userid)
        isStart = true
        //把id存起来
        saveId(userid)
    }

    override fun changeUser(newString: String) {
        mAppImp?.changeUser(newString)
        mScreenImp?.changeUser(newString)
        mDelImp?.changeUser(newString)
        //id,存起来
        saveId(newString)
    }

    override fun check() {
        if (isPause) return
        mAppImp?.check()
        mScreenImp?.check()
        mDelImp?.check()
        //DelImp不需要触发外部条件来检测
    }

    override fun pause() {
        isPause = true
        mAppImp?.pause()
        mScreenImp?.pause()
        mDelImp?.pause()
    }

    override fun resume() {
        isPause = false
        mAppImp?.resume()
        mScreenImp?.resume()
        mDelImp?.resume()
    }

    override fun stop() {
        mAppImp?.stop()
        mScreenImp?.stop()
        mDelImp?.stop()
        isStart = false
    }

    private val USERID_SAVE_KEY = "userid_save_key"

    private fun saveId(userid: String) {
        SpUtils.putString(mContext, USERID_SAVE_KEY, userid)
    }

    fun getUserId(): String {
        return SpUtils.getString(mContext, USERID_SAVE_KEY, Constant.DEFAUTL_USER_ID)
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
        mWm.removeView(mView)
        isAdd = false
    }

}


object Constant {
    val DEFAUTL_USER_ID = "no_user_id"
    val CODE_APP = 0x66
    val CODE_SCREEN = 0x67
    val CODE_DEL = 0x68
    //*************************************************************************
    val SD_PATH = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}Device${File.separator}"
    //*************************************************************************
    val ACTION_KEY = "action"
    val START_KEY = "start"
    val USER_ID_KEY = "userid"
    val STATUS_OPEN = 1
    val WOB_BLACK = 0
    val WOB_WHITE = 1
    val CHANGE_KEY = "change"
    val CODE_KEY = "code"
    val STATUS_KEY = "status"
    val WOB_KEY = "wob"
    val APPS_KEY = "apps"
    val END_KEY = "end"
    val PACKAGE_NAME_KEY = "packagename"
    val ENABLE_KEY = "enable"
    val APP_FILE_NAME = ".appcontrol"
    val SCREEN_FILE_NAME = ".screencontrol"
    val DEL_FILE_NAME = ".delcontrol"
}


class AppControlImp(context: Context, userid: String) : MyFileObserver.FileListener, ControlInterface {
    override fun pause() {
        mAppRun?.pause(true)
    }

    override fun resume() {
        mAppRun?.pause(false)
    }

    override fun change(path: String) {
        changeControl(path)
    }

    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
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

    /***
     * 触发条件时进行检测
     */
    override fun check() {
        //也应该不用实现什么
    }

    override fun changeUser(newUserid: String) {
        if (mUserId == newUserid) return
        mUserId = newUserid
        stopWathch()
        startWatch(newUserid)
    }

    override fun stop() {
        stopWathch()
        mAppRun?.setRuning(false)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}${Constant.APP_FILE_NAME}"
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
        if (mAppRun?.getRun() == true) startControl(tempContrl)
        mAppRun?.setControl(tempContrl)
    }


    class AppRunable(context: Context, control: AppControl) : Runnable {
        private var mControl = control
        private val mContext = context
        private var isRun = true
        private var isPause = false

        fun pause(pause: Boolean) {
            this.isPause = pause
        }

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
                if (mControl == null) {
                    stop()
                    break
                }
                if (mControl.status != Constant.STATUS_OPEN) {
                    stop()
                    break
                }
                val packages = mControl.apps
                if (TextUtils.isEmpty(packages)) {
                    stop()
                    break
                }
                val wob = mControl.wob
                val topApp = AppUtils.getTopPackageName(mContext)
                if (TextUtils.isEmpty(topApp)) {
                    continue
                }
                if (topApp == mContext.packageName) continue
                if (mSystemApp.contains(topApp)) continue
                when (wob) {
                    Constant.WOB_WHITE -> {
                        if (!packages.contains(topApp)) {
                            if (intime(mControl.start, mControl.end)) {
                                ControlUtils.goHome(mContext)//不在白名单
                            }
                        }
                    }
                    else -> {
                        if (packages.contains(topApp)) {
                            if (intime(mControl.start, mControl.end)) {
                                ControlUtils.goHome(mContext)//在黑名单
                            }
                        }
                    }
                }
            }
        }

        /***
         * 是否在时间内，默认都在时间内
         */
        private fun intime(start: Int, end: Int): Boolean {
            val nowTime = ControlUtils.getTime()
            return if (start in 0..24 && end in 0..24) {
                when {
                    start > end -> {
                        nowTime > end || nowTime < start
                    }
                    start < end -> {
                        nowTime in start..end
                    }
                    else -> true
                }
            } else {
                true
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

class ScreenControlImp(context: Context, userid: String) : MyFileObserver.FileListener, ControlInterface {
    override fun pause() {

    }

    override fun resume() {}

    override fun change(path: String) {
        mScreenContrl = ControlUtils.json2Screen(ControlUtils.readFile(path))
        //这个回调不是主线程
        mHandler.sendEmptyMessage(0x98)
    }

    private var mUserId = userid
    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
    private val mLockView by lazy { LockView(context) }
    private var mScreenContrl: ScreenControl? = null

    private val mHandler by lazy {
        WeakHandler(mContext.mainLooper, Handler.Callback { message ->
            if (message.what == 0x98) check()
            true
        })
    }

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        mScreenContrl = ControlUtils.json2Screen(ControlUtils.readFile(startWatch(userid)))
        check()
    }

    override fun check() {
//        doAsync {
//            var isShow = false
//            val control = mScreenContrl
//            if (control == null) {
//                isShow = false
//                return@doAsync
//            }
//            if (control.status != Constant.STATUS_OPEN) {
//                isShow = false
//                return@doAsync
//            }
//            isShow = if ((control.start in 0..24 && control.end in 0..24)) {
//                when {
//                    control.end > control.start -> ControlUtils.getTime() in control.start..control.end
//                    control.start > control.end -> {
//                        val time = ControlUtils.getTime()
//                        time > control.start || time < control.end
//                    }
//                    else -> false
//                }
//            } else {
//                false
//            }
//            uiThread {
//                if (isShow) mLockView.show()
//                else mLockView.remove()
//            }
//        }


    }

    override fun changeUser(newUserid: String) {
        if (mUserId == newUserid) return
        mUserId = newUserid
        stop()
        start(newUserid)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}${Constant.SCREEN_FILE_NAME}"
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

    override fun stop() {
        stopWathch()
        mLockView.remove()
    }
}


class DelControlImp(context: Context, userid: String) : MyFileObserver.FileListener, ControlInterface {
    override fun check() {
        //不需要实现
    }

    override fun pause() {
        mRunDelRun?.pause(true)
    }

    override fun resume() {
        mRunDelRun?.pause(false)
    }

    override fun change(path: String) {
        inCheck(mUserid, path)
    }

    private var mDelControl: DelControl? = null
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
                    if (mDelControl?.status == Constant.STATUS_OPEN) {
                        mRunDelRun = mTask.removeAt(0)
                        mSingle.execute(mRunDelRun)
                    }
                }
            }
            true
        })
    }
    private var mFileObserver: MyFileObserver? = null

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        val path = startWatch(userid)
        inCheck(userid, path)
    }

    /***
     * 这个不对外开放，因为不需要外部来检查
     */
    private fun inCheck(userid: String, path: String) {
        if (TextUtils.isEmpty(userid)) return
        if (TextUtils.isEmpty(path)) return
        val contrl = ControlUtils.json2Del(ControlUtils.readFile(path))
        mDelControl = contrl
        mRunDelRun?.setControl(contrl)
        if (contrl.status != Constant.STATUS_OPEN) {
            return
        }
        val tempList = contrl.apps
        if (tempList.isEmpty()) return
        mTask.filter { it.getControl().enable != Constant.STATUS_OPEN }
        tempList.forEach {
            mTask.add(DelRun(mContext, userid, contrl, it, mSemp, mHandler))
        }
        mHandler.sendEmptyMessage(0x99)
    }

    override fun changeUser(newUserid: String) {
        if (newUserid == mUserid) return
        mUserid = newUserid
        stop()
        start(newUserid)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}${Constant.DEL_FILE_NAME}"
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

    override fun stop() {
        stopWathch()
        mRunDelRun?.setExit(true)
        mTask.clear()
    }


    class DelRun(context: Context, userid: String, control: DelControl, delApp: App, semaphore: Semaphore, handler: WeakHandler) : Runnable {
        private val mContext = context
        private val mUserid = userid
        private val mDelApp = delApp
        private val mHandler = handler
        private val mSemap = semaphore
        private val INSTALLER_PACK = "com.android.packageinstaller"
        private var isPause = false
        fun pause(pause: Boolean) {
            this.isPause = pause
        }

        private var isRun = true
        private var mControl = control

        fun setControl(control: DelControl) {
            this.mControl = control
        }

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
                if (mControl.status != Constant.STATUS_OPEN) {
                    stop()
                    break
                }
                if (TextUtils.isEmpty(mDelApp.packagename)) {
                    stop()
                    return
                }
                if (mSystemApp.contains(mDelApp.packagename)) {
                    stop()
                    return
                }
                if (mDelApp.packagename == mContext.packageName) {
                    stop()
                    break
                }
                val isExist = AppUtils.isPackageExist(mContext, mDelApp.packagename)
                if (!isExist) {
                    stop()
                    mHandler.sendEmptyMessage(0x99)
                    break
                }
                val topApp = AppUtils.getTopPackageName(mContext)
                if (TextUtils.isEmpty(topApp)) {
                    PackageUtils.uninstallNormal(mContext, mDelApp.packagename)
                }
                if (topApp != INSTALLER_PACK) {
                    PackageUtils.uninstallNormal(mContext, mDelApp.packagename)
                }
            }
            stop()
        }


        fun setExit(pisRun: Boolean) {
            this.isRun = pisRun
        }

        private fun stop() {
            mSemap.release()
            mHandler.sendEmptyMessage(0x99)
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