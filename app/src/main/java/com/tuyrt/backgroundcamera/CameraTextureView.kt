package com.tuyrt.backgroundcamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Environment
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val error: (Throwable) -> Unit,
    val success: (String) -> Unit
) : TextureView(context, attrs, defStyleAttr), SurfaceTextureListener {

    private val TAG = "CameraTextureView"

    private var mCamera: Camera? = null
    private var rotation = 0

    private var mRatioWidth = 0
    private var mRatioHeight = 0

    init {
        surfaceTextureListener = this
    }

    fun setRotation(rotation: Int) {
        this.rotation = rotation
    }

    fun setAspectRatio(width: Int, height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth)
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height)
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        ThreadHelper.getInstance().runOnHandlerThread {
            openCamera()
            startPreviewAndTakePhoto(surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        releaseCamera()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    /**
     * 打开相机
     */
    private fun openCamera() {
        val number = Camera.getNumberOfCameras()
        val cameraInfo = CameraInfo()
        for (i in 0 until number) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                // 打开后置摄像头
                mCamera = Camera.open(i)
                setCameraDisplayOrientation(rotation, i, mCamera!!)
            }
        }
    }

    private fun setCameraDisplayOrientation(rotation: Int, cameraId: Int, camera: Camera) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> {
            }
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

    /**
     * 开始预览
     *
     * @param texture
     */
    private fun startPreviewAndTakePhoto(texture: SurfaceTexture) {
        mCamera?.let { camera ->
            try {
                camera.setPreviewTexture(texture)
                camera.startPreview()

                //拍照
                SystemClock.sleep(200)
                takePhoto(camera)
            } catch (e: IOException) {
                e.printStackTrace()
                error.invoke(IllegalStateException("Camera startPreview failed"))
            }
        } ?: error.invoke(IllegalStateException("Camera open failed"))
    }

    private fun takePhoto(camera: Camera) {
        /*
        //有的 Camera 不支持自动对焦
        val parameters = camera.parameters
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.setPreviewSize(640, 480);
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        camera.autoFocus { success, _ ->
            Log.v(TAG, "onAutoFocus====success==$success")
            Log.v(TAG, "onPictureTaken==" + Thread.currentThread().name)
            if (success) {
                camera.takePicture(null, null, { data, _ ->
                    writeDataToFile(data)
                })
            } else {
                error.invoke(IllegalStateException("Camera has not auto mode"))
            }
        }*/

        camera.takePicture(null, null, { data, _ ->
            writeDataToFile(data)
        })
    }

    private fun writeDataToFile(data: ByteArray?) {
        if (data == null) {
            error.invoke(IllegalStateException("take takePicture data is null."))
        } else {
            ThreadHelper.getInstance().execute {
                val photoDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "background")
                if (!photoDir.exists()) {
                    photoDir.mkdirs()
                }

                val file = File(photoDir, System.currentTimeMillis().toString() + ".jpg")
                FileOutputStream(file).use { output ->
                    output.write(data)
                    output.flush()
                }
                ThreadHelper.getInstance().runOnUiThread {
                    success.invoke(file.absolutePath)
                }
            }
        }
    }

    /**
     * 关闭相机
     */
    private fun releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera!!.stopPreview()
                mCamera!!.setPreviewDisplay(null)
                mCamera!!.release()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mCamera = null
        }
    }

}