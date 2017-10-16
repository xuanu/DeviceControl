package utils.zeffect.cn.controllibrary.bean

import android.text.TextUtils
import android.view.TextureView
import org.json.JSONObject
import java.io.*

data class AppControl(val code: Int,
                      val status: Int,
                      val wob: Int = 0,//白名单还是黑名单
                      val apps: String = "",
                      val start: Long = 0,
                      val end: Long = 0)

data class ScreenControl(val code: Int,
                         val status: Int,
                         val start: Long = 0,
                         val end: Long = 0)

data class DelControl(val code: Int,
                      val status: Int,
                      val apps: ArrayList<App>)

data class App(val packagename: String, val enable: Int = 0) {
    override fun toString(): String {
        return JSONObject().run { put("packagename", packagename);put("enable", enable) }.toString()
    }
}


object ControlUtils {
    fun start(userid: String = "no_user_id") {}
    fun updateUser(userid:String){}
    fun updateControl(control:String){}
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




