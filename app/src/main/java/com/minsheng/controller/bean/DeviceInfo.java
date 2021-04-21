package com.minsheng.controller.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.minsheng.controller.BR;

public class DeviceInfo extends BaseObservable {
    public int type;
    public String ip;
    public String name;
    public boolean isActivated;
    public boolean isSelected;

    public DeviceInfo(String ip, String name, int type, boolean isActivated, boolean isSelected) {
        this.ip = ip;
        this.type = type;
        this.isActivated = isActivated;
        this.isSelected = isSelected;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Bindable
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        notifyPropertyChanged(BR.ip);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    @Bindable
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
    }
}
