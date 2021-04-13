# 后台拍照
业务场景： 登录时用户无感知情况下拍摄一张照片上传后台
在服务里面创建windowManager，添加 TexttureView 用于预览之后直接拍照，在服务中监听失败或者成功，然后都关停服务。

```
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (!SmallCameraWindow.isShowing) {
        SmallCameraWindow.show(this, { e ->
            Log.d(TAG, "error is: $e")
            Toast.makeText(this, "error: $e", Toast.LENGTH_SHORT).show()
            stopSelf()
        }, { photoPath ->
            Log.d(TAG, "拍照: $photoPath")
            Toast.makeText(this, "拍照: $photoPath", Toast.LENGTH_SHORT).show()
            stopSelf()
        })
    }
    return super.onStartCommand(intent, flags, startId)
}
```