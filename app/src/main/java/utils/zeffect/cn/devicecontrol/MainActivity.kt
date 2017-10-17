package utils.zeffect.cn.devicecontrol

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import utils.zeffect.cn.controllibrary.bean.ControlUtils
import utils.zeffect.cn.controllibrary.utils.PackageUtils
import java.io.File

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startAll.setOnClickListener {
            ControlUtils.start(this)
        }
        startScreen.setOnClickListener {
            ControlUtils.change(this)
        }

        uninstallMessage.setOnClickListener {
            PackageUtils.uninstallNormal(this, "com.qimon.message")
        }

    }
}
