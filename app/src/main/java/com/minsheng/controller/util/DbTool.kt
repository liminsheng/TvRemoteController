package com.minsheng.controller.util

import androidx.databinding.BindingAdapter
import com.minsheng.controller.view.DirectionDpadView

/**
 * ClassName:      DbTool
 * Author:         LiMinsheng
 * Date:           2021/4/20 14:17
 * Description:
 */
object DbTool {

    @JvmStatic
    @BindingAdapter("setDirectionKeyListener")
    fun setDirectionKeyListener(view: DirectionDpadView, listener: DirectionDpadView.OnDirectionKeyListener) {
        view.setOnDirectionKeyListener(listener)
    }
}