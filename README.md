# DeviceControl
简单的为应用添加管控功能。

#### 问题记录
- 服务被结束后，功能会被暂停，不会重启。只有调用ControlUtils.start(context,userid),默认使用上一次的userid

####　优化方向
- 没有达到管控时间段，关掉线程。
