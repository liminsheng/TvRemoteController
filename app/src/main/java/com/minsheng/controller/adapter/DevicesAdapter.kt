package com.minsheng.controller.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.minsheng.controller.BR
import com.minsheng.controller.R
import com.minsheng.controller.bean.DeviceInfo
import com.minsheng.controller.util.NetUtils

/**
 * ClassName:      DevicesAdapter
 * Author:         LiMinsheng
 * Date:           2021/4/20 17:59
 * Description:
 */
class DevicesAdapter : RecyclerView.Adapter<DevicesAdapter.BindingHolder>() {

    var mDevices = ArrayList<DeviceInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var curDeviceInfo: DeviceInfo? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDevices.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.layout_item_device
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        val item = mDevices[position]
        holder.bindData(item)
        holder.itemView.setOnClickListener {
            if (item.isActivated && !item.isSelected) {
                item.isSelected = !item.isSelected
                if (item.isSelected) {
                    NetUtils.getInstance().ipClient = item.ip
                }
                for (deviceInfo in mDevices) {
                    if (NetUtils.getInstance().ipClient == deviceInfo.ip) {
                        deviceInfo.isSelected = true
                        curDeviceInfo = deviceInfo
                    } else {
                        deviceInfo.isSelected = false
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    fun addItem(deviceInfo: DeviceInfo) {
        if (!mDevices.contains(deviceInfo)) {
            if (deviceInfo.isSelected) {
                curDeviceInfo = deviceInfo
            }
            mDevices.add(deviceInfo)
            notifyItemInserted(mDevices.size - 1)
        } else {
            Log.e(
                "DevicesAdapter",
                "invalid device " + deviceInfo.name.toString() + "[" + deviceInfo.ip.toString() + "]"
            )
        }
    }

    fun removeItem(deviceInfo: DeviceInfo?): DeviceInfo? {
        val position = mDevices.indexOf(deviceInfo)
        if (position != -1) {
            mDevices.remove(deviceInfo)
            notifyItemRemoved(position)
            return deviceInfo
        }
        return null
    }

    fun removeItem(ip: String?): DeviceInfo? {
        var devInfo: DeviceInfo? = null
        for (deviceInfo in mDevices) {
            if (deviceInfo.ip == ip) {
                devInfo = deviceInfo
                break
            }
        }
        return devInfo?.let { removeItem(it) }
    }

    class BindingHolder(private var binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: DeviceInfo) {
            binding.setVariable(BR.device, item)
            binding.executePendingBindings()
        }
    }

}