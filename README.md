# TvRemoteController
> 一款仿悟空遥控的手机遥控器，手机安装客户端，TV安装服务端，在同一局域网内，可使用手机遥控操作TV。
1. app：客户端
2. server：服务端

## 1. 自定义遥控界面
> 高仿悟空遥控操作界面，自定义上、下、左、右、确定键View，实现上下左右扇形区域精准点击，支持长按。
## 2. 原理
> 使用`DatagramSocket`创建客户端和服务端实现通信。
- server 端启动一个前台服务，同时启动一个接收数据的线程和发送数据的线程，接收Runnable中创建一个`DatagramSocket`实例，接收指定端口（如：5555）的数据，接收到数据后解析出数据判断是否是客户端发送的，如果是客户端发送的数据，则在发送数据线程中向客户端发送自己的 ip。

```kotlin
    class ReceiverRunnable : Runnable {

        companion object {
            const val BROADCAST_PORT = 5555
        }

        @Volatile
        var isFlag = true

        override fun run() {
            val receiverBuffer = ByteArray(DATA_PACKET_SIZE)
            val datagramPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
            try {
                if (mReceiverSocket == null || mReceiverSocket?.isClosed == true) {
                    mReceiverSocket = DatagramSocket(null).run {
                        reuseAddress = true
                        bind(InetSocketAddress(BROADCAST_PORT))
                        this
                    }
                }
                while (isFlag) {
                    Log.i(TAG, "----------enter loop and wait receive data----------")
                    mReceiverSocket?.receive(datagramPacket)
                    Log.e(TAG, "receive data from ------> ${datagramPacket.address.hostAddress}")
                    mPool.submit(ParseRunnable(receiverBuffer, datagramPacket))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (mReceiverSocket?.isClosed != true) {
                    mReceiverSocket?.close()
                }
            }
        }
    }
```

- app 端进入搜索界面，创建一个发送数据线程和一个接收数据线程，发送Runnable中创建一个`DatagramSocket`，使用指定端口（如：5555）以广播的方式向局域网内的所有 ip 发送数据，接收Runnable中接收server发送的数据并解析，并展示可连接的 ip 设备列表给用户选择，用户点击对于 ip 建立连接，建立连接过程与发送广播类似，只不过现在只针对已知服务端 ip 端口发送数据，不再以广播的方式发送。

```java
				...省略
				String broadcastIp = "255.255.255.255";
                if (initClientSocket == null || initClientSocket.isClosed()) {
                    initClientSocket = new DatagramSocket(null);
                    initClientSocket.setReuseAddress(true);
                    initClientSocket.bind(new InetSocketAddress(BROADCAST_PORT));
                }

                byte[] buffer = getByteBuffer(NetConst.STTP_LOAD_TYPE_BROADCAST, 0, 0);
                int length = Build.PRODUCT.getBytes().length;
                ByteArrayInputStream bip = new ByteArrayInputStream(Build.PRODUCT.getBytes());
                bip.read(buffer, DATA_SEGMENT_START_INDEX, length);
                bip.close();

                int packetLength = DATA_PACKET_TITLE_SIZE + length;
                buffer[8] = Integer.valueOf(packetLength & 0xFF).byteValue();
                buffer[9] = Integer.valueOf((packetLength >> 8) & 0xFF).byteValue();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, packetLength,
                        InetAddress.getByName(broadcastIp), BROADCAST_PORT);
                boolean flag = false;
                while (isFlag) {/*循环发送广播,直到与TV建立连接或App退出*/
                    initClientSocket.send(datagramPacket);
                    Thread.sleep(2500);
                    Log.i(TAG, "send broadcast our ip ");
                    if (mHandler != null && !flag) {
                        mHandler.sendEmptyMessage(0);
                        flag = true;
                    }
                }
				...省略
```

- 至此 server 端和 app 端就建立起来连接，接下来 app 端主要向指定的服务端 ip 和端口发送按键数据，server 端接收客户端的数据并解析，然后实现按键注入即可。

## 3. 按键注入
> 使用`Instrumentation`发送按键

```kotlin
	private fun sendKeyDownUpSync(keyCode: Int) = runBlocking {
        launch {
            mInstrumentation.sendKeyDownUpSync(keyCode)
        }
    }
```
- 使用`Instrumentation`注入按键事件需要权限（系统级权限）：

```xml
<uses-permission android:name="android.permission.INJECT_EVENTS" />
```

- **注意事项**
  - 长按事件的实现方式是触发长按事件时，app 端向 server 端发送一个长按事件的`ACTION_DOWN`事件，此时 server 端收到事件会启动一个模拟长按的线程每隔100ms注入一次按键按下松开事件，app 端长按松开时，再向 server 端发送一个长按事件的`ACTION_UP`事件，server 端收到事件后会终止模拟长按事件的线程结束按键注入。
  - 问题点就在于，app 端需要保证`ACTION_UP`事件能够成功发送，server 端的长按事件才会结束，一些特殊情况如：app 端长按触发时崩溃、闪退、关机，都可能导致无法发出`ACTION_UP`事件，这就会导致 server 端的模拟长按线程无法结束，该键值会一直触发。可以设置超时机制解决或者其他手段避免。

## 4. Server端进程保活手段

- 前台服务
- 广播拉活
- JobService
- 全家桶相互拉活
- 账号同步机制拉活

## 5. 效果图
<img src="/snapshots/dpad.jpg" style="zoom: 30%"/>
<img src="/snapshots/search.jpg" style="zoom: 30%"/>