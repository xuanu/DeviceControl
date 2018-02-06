package utils.zeffect.cn.controllibrary.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;


/**
 * PackageUtils
 */
public class PackageUtils {
    /**
     * uninstall package normal by system intent
     *
     * @param context
     * @param packageName package name of app
     * @return whether package name is empty
     */
    public static boolean uninstallNormal(Context context, String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        Intent i = new Intent(Intent.ACTION_DELETE, Uri.parse(new StringBuilder(32).append("package:")
                .append(packageName).toString()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        return true;
    }
}