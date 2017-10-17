package utils.zeffect.cn.controllibrary.bean

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import org.jetbrains.anko.startService
import org.json.JSONObject
import utils.zeffect.cn.controllibrary.LockService
import utils.zeffect.cn.controllibrary.mvp.Constant
import java.io.*
import java.util.*


data class AppControl(val code: Int,
                      val status: Int,
                      val wob: Int = 0, //白名单还是黑名单
                      val apps: String = "",
                      val start: Long = 0,
                      val end: Long = 0)

data class ScreenControl(val code: Int,
                         val status: Int,
                         val start: Int = 0,
                         val end: Int = 0)

data class DelControl(val code: Int,
                      val status: Int,
                      val apps: List<App>)

data class App(val packagename: String, val enable: Int = 0) {
    override fun toString(): String {
        return JSONObject().run { put("packagename", packagename);put("enable", enable) }.toString()
    }
}


object ControlUtils {
    fun start(context: Context, userid: String = "no_user_id") {
        context.startService<LockService>(Pair(Constant.ACTION_KEY, Constant.START_KEY), Pair(Constant.USER_ID_KEY, userid))
    }

    fun change(context: Context) {
        context.startService<LockService>(Pair(Constant.ACTION_KEY, Constant.CHANGE_KEY))
    }

    fun getTime(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    fun updateUser(userid: String) {}
    fun updateControl(control: String) {}
    /***
     * json2应用管控bean
     */
    fun json2App(json: String): AppControl {
        return AppControl(100, 1, 0, "com.qimon.message")
    }

    fun json2Screen(json: String): ScreenControl {
        return ScreenControl(100, 0)
    }

    fun json2Del(json: String): DelControl {
        return DelControl(100, 1, Collections.emptyList<App>())
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




