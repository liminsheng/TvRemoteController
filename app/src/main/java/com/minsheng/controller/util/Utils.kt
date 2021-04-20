package com.minsheng.controller.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

/**
 * FileName:     Utils
 * Author:       LiMinsheng
 * Date:         2021/4/6 12:13
 * Description:
 */
object Utils {

    @JvmStatic
    fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
    }

    @JvmStatic
    fun getBitmap(id: Int, width: Int, resources: Resources): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, id, options)
        options.inDensity = options.outWidth
        options.inTargetDensity = width
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(resources, id, options)
    }

    @JvmStatic
    fun removeViewFormParent(v: View?) {
        if (v == null) return
        val parent = v.parent
        if (parent is ViewGroup) {
            parent.removeView(v)
        }
    }
}