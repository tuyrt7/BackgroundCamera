package com.tuyrt.backgroundcamera

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

/**
 * 隐藏的全局窗体。用于后台拍照
 */
object SmallCameraWindow {

    private val TAG = SmallCameraWindow::class.java.simpleName

    private var windowManager: WindowManager? = null
    var mTextureView: CameraTextureView? = null

    var isShowing = false

    /**
     * 显示全局窗体
     *
     * @param context
     */
    fun show(context: Context, error: (Throwable) -> Unit, success: (String) -> Unit) {
        val layoutParams: WindowManager.LayoutParams =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager!!.defaultDisplay.rotation
        mTextureView = CameraTextureView(context, error = error, success = success)
        mTextureView?.setRotation(rotation)
        layoutParams.gravity = Gravity.START or Gravity.TOP
        windowManager!!.addView(mTextureView, layoutParams)
        isShowing = true
        Log.d(TAG, "show")
    }

    /**
     * 隐藏窗体
     */
    fun dismiss() {
        if (windowManager != null && mTextureView != null) {
            windowManager!!.removeView(mTextureView)
            Log.d(TAG, "dismissed")
            mTextureView = null
            windowManager = null
        }
        isShowing = false
    }

}