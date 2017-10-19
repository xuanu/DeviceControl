package utils.zeffect.cn.controllibrary.bean

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import org.jetbrains.anko.startService
import org.json.JSONException
import org.json.JSONObject
import utils.zeffect.cn.controllibrary.LockService
import utils.zeffect.cn.controllibrary.mvp.Constant
import java.io.*
import java.util.*


data class AppControl(val code: Int,
                      val status: Int,
                      val wob: Int = 0, //白名单还是黑名单
                      val apps: String = "",
                      val start: Int = 0,
                      val end: Int = 0)

data class ScreenControl(val code: Int,
                         val status: Int,
                         val start: Int = 0,
                         val end: Int = 0)

data class DelControl(val code: Int,
                      val status: Int,
                      val apps: List<App> = Collections.emptyList<App>())

data class App(val packagename: String, val enable: Int = 0) {
    override fun toString(): String {
        return JSONObject().run { put("packagename", packagename);put("enable", enable) }.toString()
    }
}


object ControlUtils {
    fun start(context: Context, userid: String = "") {
        context.startService<LockService>(Pair(Constant.ACTION_KEY, Constant.START_KEY), Pair(Constant.USER_ID_KEY, userid))
    }

    fun getTime(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    fun updateUser(userid: String) {}
    fun updateControl(userid: String, control: String) {
        if (TextUtils.isEmpty(userid)) return
        if (TextUtils.isEmpty(control)) return
        try {
            val dataJson = JSONObject(control)
            val code = dataJson.getInt(Constant.CODE_KEY)
            when (code) {
                Constant.CODE_APP -> {
                    write("${Constant.SD_PATH}$userid${File.separator}${Constant.APP_FILE_NAME}", control)
                }
                Constant.CODE_SCREEN -> {
                    write("${Constant.SD_PATH}$userid${File.separator}${Constant.SCREEN_FILE_NAME}", control)
                }
                Constant.CODE_DEL -> {
                    write("${Constant.SD_PATH}$userid${File.separator}${Constant.DEL_FILE_NAME}", control)
                }
            }
        } catch (e: JSONException) {

        }

    }

    /***
     * json2应用管控bean
     */
    fun json2App(json: String): AppControl {
        val defaultControl = AppControl(Constant.CODE_APP, 0, 0, "")
        try {
            val dataJson = JSONObject(json)
            val code = dataJson.getInt(Constant.CODE_KEY)
            if (code != Constant.CODE_APP) return defaultControl
            val status = dataJson.getInt(Constant.STATUS_KEY)
            val wob = dataJson.getInt(Constant.WOB_KEY)
            val apps = dataJson.getString(Constant.APPS_KEY)
            val start = if (!dataJson.isNull(Constant.START_KEY)) dataJson.getInt(Constant.START_KEY) else 0
            val end = if (!dataJson.isNull(Constant.END_KEY)) dataJson.getInt(Constant.END_KEY) else 0
            return AppControl(code, status, wob, apps, start, end)
        } catch (e: JSONException) {
            return defaultControl
        }
    }

    fun json2Screen(json: String): ScreenControl {
        val defaultControl = ScreenControl(Constant.CODE_SCREEN, 0)
        try {
            val dataJson = JSONObject(json)
            val code = dataJson.getInt(Constant.CODE_KEY)
            if (code != Constant.CODE_SCREEN) return defaultControl
            val status = dataJson.getInt(Constant.STATUS_KEY)
            val start = if (!dataJson.isNull(Constant.START_KEY)) dataJson.getInt(Constant.START_KEY) else 0
            val end = if (!dataJson.isNull(Constant.END_KEY)) dataJson.getInt(Constant.END_KEY) else 0
            return ScreenControl(code, status, start, end)
        } catch (e: JSONException) {
            return defaultControl
        }
    }

    fun json2Del(json: String): DelControl {
        val defaultControl = DelControl(Constant.CODE_DEL, 0)
        try {
            val dataJson = JSONObject(json)
            val code = dataJson.getInt(Constant.CODE_KEY)
            if (code != Constant.CODE_DEL) return defaultControl
            val status = dataJson.getInt(Constant.STATUS_KEY)
            val apps = arrayListOf<App>()
            val dataArray = dataJson.getJSONArray(Constant.APPS_KEY)
            for (i in 0 until dataArray.length()) {
                val appJson = dataArray.getJSONObject(i)
                val enable = if (!appJson.isNull(Constant.ENABLE_KEY)) appJson.getInt(Constant.ENABLE_KEY) else 0
                apps.add(App(appJson.getString(Constant.PACKAGE_NAME_KEY), enable))
            }
            return DelControl(code, status, apps)
        } catch (e: JSONException) {
            return defaultControl
        }
    }

    /***
     * 去桌面
     */
    fun goHome(context: Context) {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(homeIntent)
    }

    /****
     * 读取文件内容
     * @param filePath 路径
     */
    fun readFile(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) return ""
        val tempFile = File(filePath)
        if (!tempFile.exists() || tempFile.isDirectory) return ""
        val fileInput = FileInputStream(tempFile)
        return inputStream2String(fileInput)
    }

    /***
     * 写文件内容
     * @param filePath 路径
     * @param content 内容
     * @param append 是否追加
     */
    fun write(filePath: String, content: String, append: Boolean = false): Boolean {
        try {
            if (TextUtils.isEmpty(filePath)) return false
            val tempFile = File(filePath)
            if (!tempFile.exists() || tempFile.isDirectory) return false
            val writer = FileWriter(filePath, append)
            writer.write(content)
            writer.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }


    }

    private fun inputStream2String(inputs: InputStream): String {
        val baos = ByteArrayOutputStream()
        try {
            var i = inputs.read()
            while (i != -1) {
                baos.write(i)
                i = inputs.read()
            }
        } catch (e: IOException) {
        } finally {
            return baos.toString()
        }
    }

}




