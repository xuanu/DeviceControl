# DeviceControl
简单的为应用添加管控功能。

### 更新说明：
> 20180206更新：增加辅助服务：成长伴侣，用于获取顶层包名，和自动卸载APP。
> 使用;ControlUtils.goAccess(context)去打开辅助功能
> 全用：ControlUtils.checkAccessibilityEnabled(context) 检查服务是否开启。

### 使用说明
1. 开启服务 `ControlUtils.start(this, USERID)`
2. 修改策略 `ControlUtils.updateControl(USERID, ""`);//参考各个策略的数据结构
3. 修改用户 `ControlUtils.updateUser(context,NEWUSERID)`,也可以重复调用`ControlUtils.start(this, NEW_USERID)`

> 一定要获取这个权限：[安卓5.0上下应用锁的实现方法](http://www.jianshu.com/p/6692f41bcc67)
> 包名我自己拿，但权限要你去申请。

## 说明
1. 应用锁
2. 设备锁 (可用想的R.layout.layout_control_screen替换默认的锁屏布局)，长按60锁可实现暂时解锁。
3. 卸载应用
### 1. 应用锁或游戏锁
  > 白名单（仅白名单内应用可运行）

  > 黑名单  （仅黑名单内应用不可运行）

> 一定要注意这个操作：**开线程检测顶层运行包名，执行操作。获取顶层包名可参考5.0上下 ：[安卓5.0上下应用锁的实现方法](http://www.jianshu.com/p/6692f41bcc67)**

##### 应用锁或游戏数据结构
```
{
    "code":102,//指定的
    "start":0,//开始时间0-24
    "status":1,//0关、1开
    "end":24,//结束时间0-24
    "wob":0,//0黑名单、1白名单
    "apps":"com.qimon.message,qimon.com.cn.qimoncheck"//要管控的应用,分隔
}
```


### 2. 设备锁
  > WindowManager实现

```
{
    "start":0,
    "status":1,
    "end":24,
    "code":103
}
```


### 3. 卸载应用
> 开线程，循环卸载应用

```
{
    "status":1,
    "apps":"com.qimon.message,qimon.com.cn.qimoncheck",
    "code":104
}
```
