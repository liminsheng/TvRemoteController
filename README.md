# TvRemoteController
> 一款仿悟空遥控的手机遥控器，手机安装客户端，TV安装服务端，在同一局域网内，可使用手机遥控操作TV。
## 1. 自定义遥控界面
> 高仿悟空遥控操作界面，自定义上、下、左、右、确定键View，实现精准点击，支持长按。
## 2. 数据传输
> 使用DatagramSocket创建客户端和服务端实现通信
## 3. 按键注入
> 使用`Instrumentation`发送按键

```
private fun sendKeyDownUpSync(keyCode: Int) = runBlocking {
        launch {
            mInstrumentation.sendKeyDownUpSync(keyCode)
        }
    }
```
## 4. 效果图
