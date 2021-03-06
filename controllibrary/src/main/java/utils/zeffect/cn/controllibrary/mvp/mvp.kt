package utils.zeffect.cn.controllibrary.mvp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.WindowManager
import utils.zeffect.cn.controllibrary.R
import utils.zeffect.cn.controllibrary.accessibility.AppAccessService
import utils.zeffect.cn.controllibrary.bean.AppControl
import utils.zeffect.cn.controllibrary.bean.DelControl
import utils.zeffect.cn.controllibrary.bean.InControlUtils
import utils.zeffect.cn.controllibrary.bean.ScreenControl
import utils.zeffect.cn.controllibrary.utils.PackageUtils
import utils.zeffect.cn.controllibrary.utils.WeakHandler
import zeffect.cn.common.app.AppUtils
import zeffect.cn.common.log.L
import zeffect.cn.common.sp.SpUtils
import java.io.File
import java.util.concurrent.Executors


interface ControlInterface {
    fun changeUser(newUserid: String)
    fun check()
    fun pause()
    fun resume()
    fun stop()
    fun packageAdd(name: String) {}
    fun checkTop(top: String)
}

class LockImp(context: Context) : ControlInterface {
    override fun checkTop(top: String) {
        mAppImp?.checkTop(top)
    }

    private val mContext by lazy { context }
    private var isStart = false
    private var mAppImp: AppControlImp? = null
    private var mScreenImp: ScreenControlImp? = null
    private var mDelImp: DelControlImp? = null
    private var isPause = false
    fun start(userid: String = Constant.DEFAUTL_USER_ID) {
        //想了一下，可以重复start，只要id不同
        L.e("LockImp start userid:$userid")
        if (TextUtils.isEmpty(userid) && isStart) return
        if (getUserId() == userid && isStart) return
        stop()
        isPause = false
        mAppImp = AppControlImp(mContext, userid)
        mScreenImp = ScreenControlImp(mContext, userid)
        mDelImp = DelControlImp(mContext, userid)
        isStart = true
        //把id存起来
        saveId(userid)
        L.e("LockImp start success")
    }


    override fun packageAdd(name: String) {
        mDelImp?.packageAdd(name)
    }

    override fun changeUser(newString: String) {
        L.e("LockImp changeUser :$newString")
        mAppImp?.changeUser(newString)
        mScreenImp?.changeUser(newString)
        mDelImp?.changeUser(newString)
        //id,存起来
        saveId(newString)
    }

    override fun check() {
        L.e("LockImp start check")
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
        L.e("LockImp stop")
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
            this.alpha = 0.8f
            this.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            this.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
    }
    private val mView by lazy {
        LayoutInflater.from(context).inflate(R.layout.layout_control_screen, null).apply {
            //TODO 测试期间，点击移除
            var mStart: Long = 0; this.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.KeyEvent.ACTION_DOWN -> {
                    mStart = System.currentTimeMillis()
                }
                android.view.KeyEvent.ACTION_UP -> {
                    if ((System.currentTimeMillis()) - mStart > 60 * 1000) remove()
                }
            }
            ;true
        }
        }
    }

    init {
        mWm
        mParam
        mView
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
    val ACTION_SHOW_VIEW_KEY = "action_show_view"
    val ACTION_REMOVE_VIEW_KEY = "action_remove_view"
    //*************************************************************************
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
        L.e("AppControlImp receiver change path :$path")
        changeControl(path)
    }

    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
    private val mSingleThreadExecutor = Executors.newSingleThreadExecutor()
    private var mAppRun: AppRunable? = null
    private var mUserId = userid
    private var mControl: AppControl? = null

    init {
        start(userid)
    }

    private fun start(userid: String) {
        stop()
        val path = startWatch(userid)
        startControl(InControlUtils.json2App(InControlUtils.readFile(path)))
    }

    /***
     * 触发条件时进行检测
     */
    override fun check() {
        if (InControlUtils.intime(mControl?.start ?: 0, mControl?.end ?: 0)) {
            val path = "${Constant.SD_PATH}$mUserId${File.separator}${Constant.APP_FILE_NAME}"
            changeControl(path)
        }
    }

    /***
     * 检查顶层包名
     */
    override fun checkTop(top: String) {
        if (top.isNullOrBlank()) return
        mAppRun?.checkTop(top)
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
        L.e("AppControlImp watch path :$path")
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
        mControl = mAppContrl
        if (mAppRun == null || !mAppRun?.getRun()!!) mAppRun = AppRunable(mContext, mAppContrl)
        mSingleThreadExecutor.execute(mAppRun)
    }

    private fun changeControl(path: String) {
        val tempContrl = InControlUtils.json2App(InControlUtils.readFile(path))
        if (mAppRun?.getRun() == true) startControl(tempContrl)
        mAppRun?.setControl(tempContrl)
        if (mAppRun == null || mAppRun?.getRun() == false) {
            startControl(tempContrl)
        }
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
            L.e("AppRunable run")
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
                if (TextUtils.isEmpty(packages) || !InControlUtils.intime(mControl.start, mControl.end)) {
                    stop()
                    break
                }
                val wob = mControl.wob
                val topApp = AppUtils.getTopPackageName(mContext)
                L.e("AppRun top app :$topApp")
                if (TextUtils.isEmpty(topApp)) {
                    continue
                }
                if (topApp == mContext.packageName) continue
                if (mSystemApp.contains(topApp)) continue
                when (wob) {
                    Constant.WOB_WHITE -> {
                        if (!packages.contains(topApp)) {
                            InControlUtils.goHome(mContext)//不在白名单
                        }
                    }
                    else -> {
                        if (packages.contains(topApp)) {
                            InControlUtils.goHome(mContext)//在黑名单
                        }
                    }
                }
            }
            L.e("AppRunable finish")
        }


        fun checkTop(top: String) {
            object : AsyncTask<String, Void, Boolean>() {
                override fun doInBackground(vararg p0: String?): Boolean {
                    if (top.isNullOrBlank()) return false
                    if (isPause) return false
                    if (mControl == null) {
                        return false
                    }
                    if (mControl.status != Constant.STATUS_OPEN) {
                        return false
                    }
                    val packages = mControl.apps
                    if (TextUtils.isEmpty(packages) || !InControlUtils.intime(mControl.start, mControl.end)) {
                        return false
                    }
                    val wob = mControl.wob
                    val topApp = top
                    L.e("AppRun top app :$topApp")
                    if (TextUtils.isEmpty(topApp)) {
                        return false
                    }
                    if (topApp == mContext.packageName) return false
                    if (mSystemApp.contains(topApp)) return false
                    when (wob) {
                        Constant.WOB_WHITE -> {
                            if (!packages.contains(topApp)) {
                                return true
                            }
                        }
                        else -> {
                            if (packages.contains(topApp)) {
                                return true
                            }
                        }
                    }
                    return false
                }

                override fun onPostExecute(result: Boolean?) {
                    if (result == true) InControlUtils.goHome(mContext)//去桌面
                }
            }.execute(top)
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
    override fun checkTop(top: String) {

    }

    override fun pause() {

    }

    override fun resume() {}

    override fun change(path: String) {
        L.e("ScreenControlImp change path:$path")
        mScreenContrl = InControlUtils.json2Screen(InControlUtils.readFile(path))
        //这个回调不是主线程
        mHandler.sendEmptyMessage(0x98)
    }

    private var mUserId = userid
    private val mContext by lazy { context }
    private var mFileObserver: MyFileObserver? = null
    private var mScreenContrl: ScreenControl? = null
    private val mLockView by lazy { LockView(mContext) }

    private val mHandler by lazy {
        WeakHandler(mContext.mainLooper, Handler.Callback { message ->
            if (message.what == 0x98) check()
            true
        })
    }

    init {
        start(userid)
        mLockView
    }

    private fun start(userid: String) {
        stop()
        mScreenContrl = InControlUtils.json2Screen(InControlUtils.readFile(startWatch(userid)))
        check()
    }

    override fun check() {
        L.e("ScreenConrolImp check")
        var isShow = false
        val control = mScreenContrl
        if (control == null || control.status != Constant.STATUS_OPEN) {
            isShow = false
        } else {
            isShow = if ((control.start in 0..24 && control.end in 0..24)) {
                when {
                    control.end > control.start -> InControlUtils.getTime() in control.start..control.end
                    control.start > control.end -> {
                        val time = InControlUtils.getTime()
                        time > control.start || time < control.end
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        if (isShow) mLockView.show()
        else mLockView.remove()
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
    }
}


class DelControlImp(context: Context, userid: String) : MyFileObserver.FileListener, ControlInterface {
    override fun checkTop(top: String) {
    }

    override fun check() {
        //不需要实现
    }

    override fun packageAdd(name: String) {
        val path = "${Constant.SD_PATH}$mUserid${File.separator}${Constant.DEL_FILE_NAME}"
        inCheck(mUserid, path)
    }

    override fun pause() {
        mRunDelRun?.pause(true)
    }

    override fun resume() {
        mRunDelRun?.pause(false)
    }

    override fun change(path: String) {
        L.e("DelControlImp change path $path")
        inCheck(mUserid, path)
    }

    private var mDelControl: DelControl? = null
    private var mUserid = userid
    private val mContext = context
    private val mSingle by lazy { Executors.newSingleThreadExecutor() }
    private var mRunDelRun: DelRun? = null
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
        val contrl = InControlUtils.json2Del(InControlUtils.readFile(path))
        mDelControl = contrl
        mRunDelRun?.setControl(contrl)
        if (contrl.status != Constant.STATUS_OPEN) {
            return
        }
        val tempList = contrl.apps
        if (tempList.isEmpty()) return
        checkApp()
    }


    private fun checkApp() {
        if (mDelControl == null) {
            mRunDelRun?.setExit(true);return
        }
        if (mDelControl?.status != Constant.STATUS_OPEN) {
            mRunDelRun?.setExit(true);return
            return
        }
        if (mDelControl?.apps?.isEmpty() == true) {
            mRunDelRun?.setExit(true);return
            return
        }
        mRunDelRun = DelRun(mContext, mUserid, mDelControl!!)
        mSingle.execute(mRunDelRun)
    }

    override fun changeUser(newUserid: String) {
        if (newUserid == mUserid) return
        mUserid = newUserid
        stop()
        start(newUserid)
    }

    private fun startWatch(userid: String): String {
        val path = "${Constant.SD_PATH}$userid${File.separator}${Constant.DEL_FILE_NAME}"
        L.e("DelControlImp watch path $path")
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
    }

    /***
     * 这个线程，只有当需要卸载的内容为空，或者，开关为关，才结束
     */
    class DelRun(context: Context, userid: String, control: DelControl) : Runnable {
        private val mContext = context
        private val mUserid = userid
        private val INSTALLER_PACK = "com.android.packageinstaller"
        private var isPause = false
        fun pause(pause: Boolean) {
            this.isPause = pause
        }

        private var isRun = true
        private var mControl = control
        private var mDelApps = arrayListOf<String>()


        fun setControl(control: DelControl) {
            this.mControl = control
            mDelApps.clear()
            mDelApps.addAll(anyApps())
            L.e("DelRun control status ${this.mControl.status},recevier status:${control.status}")
        }

        private val mSystemApp by lazy {
            val tempSys = StringBuilder()
            AppUtils.getApps(mContext, 1).forEach { tempSys.append(it.packageName + ",") }
            tempSys
        }

        init {
            mDelApps.clear()
            mDelApps.addAll(anyApps())
        }

        private fun anyApps(): List<String> {
            if (mControl == null) return emptyList()
            if (mControl.status != Constant.STATUS_OPEN) return emptyList()
            val apps = mControl.apps
            if (TextUtils.isEmpty(apps)) return emptyList()
            return apps.split(",")
        }

        override fun run() {
            L.e("DelRun run")
            if (mControl == null) {
                stop()
                return
            }
            if (TextUtils.isEmpty(mControl.apps) || mDelApps.isEmpty()) {
                stop()
                return
            }
            val lastUserId = mUserid
            while (isRun) {
                L.e("DelRun control status ${this.mControl.status}")
                if (lastUserId != mUserid || mControl == null || mDelApps.isEmpty() || mControl.status != Constant.STATUS_OPEN) {
                    stop()
                    break
                }
                Thread.sleep(5000)
                if (isPause) {
                    continue
                }
                if (mDelApps.isEmpty()) {
                    stop()
                    break
                }
                val delPackageName = mDelApps[0]
                L.e("del app $delPackageName")
                if (TextUtils.isEmpty(delPackageName) || mSystemApp.contains(delPackageName) || delPackageName == mContext.packageName || !AppUtils.isPackageExist(mContext, delPackageName)) {
                    mDelApps.removeAt(0)
                    continue
                }

                val topApp = AppUtils.getTopPackageName(mContext)
                L.e("DelRun top app :$topApp")
                if (TextUtils.isEmpty(topApp)) {
                    mContext.sendBroadcast(Intent(AppAccessService.ACTION_ACTIVE_AUTO_INSTALL_APK).putExtra(AppAccessService.ACTIVE_TIME, System.currentTimeMillis()))
                    PackageUtils.uninstallNormal(mContext, delPackageName)
                }
                if (topApp != INSTALLER_PACK) {
                    mContext.sendBroadcast(Intent(AppAccessService.ACTION_ACTIVE_AUTO_INSTALL_APK).putExtra(AppAccessService.ACTIVE_TIME, System.currentTimeMillis()))
                    PackageUtils.uninstallNormal(mContext, delPackageName)
                }
            }
            L.e("DelRun finish")
        }


        fun setExit(pisRun: Boolean) {
            this.isRun = pisRun
        }

        private fun stop() {
            this.isRun = false
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