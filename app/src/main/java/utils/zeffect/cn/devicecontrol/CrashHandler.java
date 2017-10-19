package utils.zeffect.cn.devicecontrol;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 应用奔溃信息收集类。
 *
 * @author fanjiao
 */
public class CrashHandler implements UncaughtExceptionHandler {
    /**
     * TAG
     */
    public static final String TAG = "QimonCaught";
    /**
     * 系统默认的UncaughtException处理类
     */
    private UncaughtExceptionHandler mDefaultHandler;
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE = new CrashHandler();
    /**
     * 程序的Application对象
     */
    private Application application;
    /**
     * 应用名
     */
    private String AppName = "";
    /**
     * 应用版本号
     */
    private String AppVersion = "";
    /**
     * 应用包名
     */
    private String AppPackage = "";
    /**
     * 手机IMEI号
     */
    private String imei = "";
    /**
     * 报错时间
     */
    private String errorTime = "";
    /**
     * 报错信息
     */
    private String exInfo = "";
    /**
     * 手机信息
     */
    private String deviceInfo = "";


    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     *
     * @return 错误信息收集类
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param application BaseApplication
     */
    public void init(Application application) {
        this.application = application;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, TAG + "报错退出！！！！！！！");
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            if (ex != null) {
                ex.printStackTrace();
            }
            // 退出程序
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 错误信息
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 使用Toast来显示异常信息
        // new Thread() {
        // @Override
        // public void run() {
        // Looper.prepare();
        // Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.",
        // Toast.LENGTH_LONG).show();
        // Looper.loop();
        // }
        // }.start();
        // 收集设备参数信息
        deviceInfo = CollectDeviceInfo();
        // 收集应用信息
        CollectAppInfo(application);
        // 收集报错信息
        exInfo = CollectExecpitonInfo(ex);
        // 构建其他信息。
        BuidingOtherInfo();
        // 发送log
        SendService();
        return true;
    }

    /**
     * 发送报错log
     */
    private void SendService() {
        //调用报错日志专用通道。
        //CrashLogSendUtil.sendCrashLogContext(application, map, new UserData(application, DataConstant.student).getPeople());
    }

    /**
     * 收集其他信息。
     */
    private void BuidingOtherInfo() {

    }

    /**
     * 收集报错信息<网上找的>
     *
     * @param ex 错误对象
     * @return 错误信息字符串
     */
    public static String CollectExecpitonInfo(Throwable ex) {
        if (ex == null) {
            return "";
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }

    /**
     * 收集报错信息<自己写的,备份留用，暂时未使用>
     *
     * @param ex 错误对象
     * @return 错误信息字符串
     */
    private String CollectExecpitonInfo2(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        if (ex != null) {
            StackTraceElement[] stEle = ex.getStackTrace();
            sb.append(ex.getClass()).append("\n");
            for (int i = 0; i < stEle.length; i++) {
                sb.append(stEle[i].toString()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 收集app信息
     *
     * @param ctx 上下文
     */
    private void CollectAppInfo(Context ctx) {
//        if (ctx != null) {
//            AppPackage = ctx.getPackageName();
//            ApkManager appInfoManager = null;
//            try {
//                appInfoManager = new ApkManager("", ctx);
//            } catch (NameNotFoundException e1) {
//                e1.printStackTrace();
//            }
//            try {
//                if (appInfoManager != null) {
//                    AppName = appInfoManager.getAppName();
//                    AppVersion = appInfoManager.getAppVersion();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
    }

    /**
     * 收集设备参数信息
     *
     * @return 设备信息字符串
     */
    public String CollectDeviceInfo() {
        return "";
//        // 用来存储设备信息
//        Map<String, String> infos = new HashMap<>();
//        Field[] fields = Build.class.getDeclaredFields();
//        for (Field field : fields) {
//            try {
//                field.setAccessible(true);
//                String key = DeviceInfo.Param.SwitchValue4Key(field.getName());
//                String val = field.get(null).toString();
//                if (key != null && val != null) {
//                    infos.put(key, val);
//                }
//                Log.d(TAG, field.getName() + " : " + field.get(null));
//            } catch (Exception e) {
//                Log.e(TAG, "an error occured when collect crash info", e);
//            }
//        }
//        infos.put(DeviceInfo.Param.SwitchValue4Key("SDK"), Build.VERSION.SDK_INT + "");
//        infos.put(DeviceInfo.Param.SwitchValue4Key("RELEASE"), Build.VERSION.RELEASE);
//        String info = infos.toString();
//        return info.substring(info.indexOf("{") + 1, info.lastIndexOf("}"));
    }

}
