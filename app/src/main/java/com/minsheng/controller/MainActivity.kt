package com.minsheng.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.minsheng.controller.databinding.ActivityMainBinding
import com.minsheng.controller.util.NetUtils
import com.minsheng.controller.view.DirectionDpadView

class MainActivity : AppCompatActivity() {

    private lateinit var mDataBinding: ActivityMainBinding
    private val mLiveData = MutableLiveData<String>()
    private val mVibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        mDataBinding.mToolbar.subtitle = NetUtils.getInstance().ipClient ?: ""
    }

    private fun initView() {
        mLiveData.observe(this, Observer {
            mDataBinding.tips = it
            mVibrator.vibrate(100)
        })
        mDataBinding.apply {
            mToolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_search) {
                    val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
                    startActivity(intent)
                }
                true
            }
            setOnShotDownClick {
                mLiveData.value = "您点击了关机键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_POWER)
            }
            setOnVolumeDownClick {
                mLiveData.value = "您点击了音量减键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_VOLUME_DOWN)
            }
            setOnVolumeUpClick {
                mLiveData.value = "您点击了音量加键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_VOLUME_UP)
            }
            setOnHomeClick {
                mLiveData.value = "您点击了主页键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_HOME)
            }
            setOnMenuClick {
                mLiveData.value = "您点击了菜单键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_MENU)
            }
            setOnBackClick {
                mLiveData.value = "您点击了返回键"
                NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_BACK)
            }
            directionDpadListener = object : DirectionDpadView.OnDirectionKeyListener {
                override fun onClick(keyCode: Int) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            mLiveData.value = "您点击了确定键"
                            NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_DPAD_CENTER)
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            mLiveData.value = "您点击了上键"
                            NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_DPAD_UP)
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            mLiveData.value = "您点击了下键"
                            NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_DPAD_DOWN)
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            mLiveData.value = "您点击了左键"
                            NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_DPAD_LEFT)
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            mLiveData.value = "您点击了右键"
                            NetUtils.getInstance().sendKey(KeyEvent.KEYCODE_DPAD_RIGHT)
                        }
                    }
                }

                override fun onLongPress(keyCode: Int, action: Boolean) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            mLiveData.value = "您长按了确定键 ${if (action) "" else "松开"}"
                            NetUtils.getInstance().sendLongKey(KeyEvent.KEYCODE_DPAD_CENTER, action)
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            mLiveData.value = "您长按了上键 ${if (action) "" else "松开"}"
                            NetUtils.getInstance().sendLongKey(KeyEvent.KEYCODE_DPAD_UP, action)
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            mLiveData.value = "您长按了下键 ${if (action) "" else "松开"}"
                            NetUtils.getInstance().sendLongKey(KeyEvent.KEYCODE_DPAD_DOWN, action)
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            mLiveData.value = "您长按了左键 ${if (action) "" else "松开"}"
                            NetUtils.getInstance().sendLongKey(KeyEvent.KEYCODE_DPAD_LEFT, action)
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            mLiveData.value = "您长按了右键 ${if (action) "" else "松开"}"
                            NetUtils.getInstance().sendLongKey(KeyEvent.KEYCODE_DPAD_RIGHT, action)
                        }
                    }

                }
            }
        }
    }

    private fun initListener() {
        if (!NetUtils.getInstance().isConnectToClient) {
            val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        NetUtils.getInstance().release()
    }
}