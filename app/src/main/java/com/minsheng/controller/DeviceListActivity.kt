package com.minsheng.controller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.minsheng.controller.adapter.DevicesAdapter
import com.minsheng.controller.bean.DeviceInfo
import com.minsheng.controller.databinding.ActivityDeviceListBinding
import com.minsheng.controller.util.ConfigConst
import com.minsheng.controller.util.DevicesUtil
import com.minsheng.controller.util.NetUtils

/**
 * ClassName:      DeviceListActivity
 * Author:         LiMinsheng
 * Date:           2021/4/20 19:18
 * Description:
 */
class DeviceListActivity : AppCompatActivity() {

    private lateinit var mDataBinding: ActivityDeviceListBinding
    private val mLiveData = MutableLiveData<String>()
    private val mDeviceAdapter by lazy {
        DevicesAdapter()
    }

    private val mNetHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what and 0xFF) {
                ConfigConst.MSG_INIT_CONNECTION -> Toast.makeText(
                    this@DeviceListActivity, "初始化完成，查找设备...",
                    Toast.LENGTH_SHORT
                ).show()
                ConfigConst.MSG_FIND_OUT_DEVICE -> {
                    mDeviceAdapter.addItem(msg.obj as DeviceInfo)
                    mLiveData.value = "已经发现：${mDeviceAdapter.itemCount} 台设备"
                    if (hasMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)) {
                        removeMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)
                    }
                }
                ConfigConst.MSG_DISCONNECTION -> Toast.makeText(
                    this@DeviceListActivity, "连接已经断开，App不可用。",
                    Toast.LENGTH_SHORT
                ).show()
                ConfigConst.MSG_THERE_NO_CONNECTION_TO_DEVICE -> Toast.makeText(
                    this@DeviceListActivity, "未连接到TV，App不可用。",
                    Toast.LENGTH_SHORT
                ).show()
                ConfigConst.MSG_INVALIED_INPUT_TEXT -> Toast.makeText(
                    this@DeviceListActivity, "输入无效，请重新输入！",
                    Toast.LENGTH_SHORT
                ).show()
                ConfigConst.MSG_CONNECTION_TIME_OUT -> {
                    Toast.makeText(
                        this@DeviceListActivity, "连接超时......",
                        Toast.LENGTH_SHORT
                    ).show()
                    mLiveData.value = ""
                    mDataBinding.mRefreshLayout.finishRefresh()
                    NetUtils.getInstance().stopInitClient()
                }
                ConfigConst.MSG_DEVICE_DISCONNECTION -> {
                    val ip = msg.obj as String
                    val deviceInfo = mDeviceAdapter.removeItem(ip)
                    if (deviceInfo != null) {
                        Toast.makeText(
                            this@DeviceListActivity,
                            "设备：" + deviceInfo.name.toString() + "[" + deviceInfo.ip.toString() + "] 离线。",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                ConfigConst.MSG_EXCEPTION -> Toast.makeText(
                    this@DeviceListActivity, "出现异常，无法操作",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_device_list)
        initView()
        if (!NetUtils.getInstance().isConnectToClient) {
            mDataBinding.mRefreshLayout.autoRefresh()
        } else if (DevicesUtil.mDevicesList.isNotEmpty()) {
            mDeviceAdapter.mDevices = DevicesUtil.mDevicesList
        }
    }

    private fun initView() {
        mLiveData.observe(this, Observer {
            mDataBinding.searchStatus = it
        })
        mDataBinding.apply {
            mToolbar.setNavigationOnClickListener {
                finish()
            }
            val linearLayoutManager =
                LinearLayoutManager(this@DeviceListActivity, LinearLayoutManager.VERTICAL, false)
            mRvDevice.layoutManager = linearLayoutManager
            mRvDevice.addItemDecoration(
                DividerItemDecoration(
                    this@DeviceListActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
            mRvDevice.adapter = mDeviceAdapter
            mRefreshLayout.setOnRefreshListener {
                searchDevices()
            }
        }
    }

    private fun searchDevices() {
        mLiveData.value = "正在搜索设备..."
        val msg: Message = mNetHandler.obtainMessage()
        msg.what = ConfigConst.MSG_CONNECTION_TIME_OUT
        mNetHandler.sendMessageDelayed(msg, 15000)
        NetUtils.getInstance().init(mNetHandler)
    }

    override fun onStop() {
        super.onStop()
        DevicesUtil.mDevicesList = mDeviceAdapter.mDevices
        NetUtils.getInstance().stopInitClient()
        if (mNetHandler.hasMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)) {
            mNetHandler.removeMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)
        }
    }
}