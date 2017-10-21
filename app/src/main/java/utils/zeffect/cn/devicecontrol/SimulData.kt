package utils.zeffect.cn.devicecontrol

import org.json.JSONArray
import org.json.JSONObject
import utils.zeffect.cn.controllibrary.bean.ScreenControl
import utils.zeffect.cn.controllibrary.mvp.Constant

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/10/18
 *      desc    ：
 *      version:：1.0
 * </pre>
 * @author zzx
 */
/**
 * 模拟数据
 */
object SimulData {
    fun App(status: Int = 1, wob: Int = 0): String {
        val dataJson = JSONObject()
        dataJson.put("code", Constant.CODE_APP)
        dataJson.put("status", status)
        dataJson.put("wob", wob)
        dataJson.put("apps", "com.qimon.message,qimon.com.cn.qimoncheck")
        dataJson.put("start", 0)
        dataJson.put("end", 24)
        return dataJson.toString()
    }

    fun ScreenControl(status: Int = 1): String {
        val dataJson = JSONObject()
        dataJson.put("code", Constant.CODE_SCREEN)
        dataJson.put("status", status)
        dataJson.put("start", 0)
        dataJson.put("end", 24)
        return dataJson.toString()
    }

    fun DekControl(status: Int = 1): String {
        val dataJson = JSONObject()
        dataJson.put("code", Constant.CODE_DEL)
        dataJson.put("status", status)
        dataJson.put("apps", "com.qimon.message,qimon.com.cn.qimoncheck")
        return dataJson.toString()
    }


}
