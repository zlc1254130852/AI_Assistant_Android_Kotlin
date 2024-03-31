package com.example.AI_Assistant

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.tiia.v20190529.TiiaClient
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelProRequest
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelProResponse
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream


class ObjDetectActivity: Activity() {
    val takePhoto = 1
    val fromAlbum = 2
    lateinit var imageUri: Uri
    lateinit var outputImage: File
    lateinit var imageView: ImageView
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_layout)

        var takePhotoBtn = findViewById<View>(R.id.takePhotoBtn)
        var fromAlbumBtn = findViewById<View>(R.id.fromAlbumBtn)
        var detectBtn = findViewById<View>(R.id.detectBtn)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

//        window.setBackgroundDrawableResource(R.color.colorPrimary)
//        val nightModeSwitch = NightModeSwitch(this,window)

        takePhotoBtn.setOnClickListener {

            outputImage = File(externalCacheDir, "output_image.jpg")
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 FileProvider.getUriForFile(this, "com.example.AI_Assistant.fileprovider", outputImage)
            } else {
                Uri.fromFile(outputImage)
            }
            // 启动相机程序
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, takePhoto)
        }

        fromAlbumBtn.setOnClickListener {
            // 打开文件选择器
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // 指定只显示图片
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
        }

        detectBtn.setOnClickListener {

                // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey
                val cred = Credential(
                    "",
                    ""
                )
                // 实例化一个http选项，可选的，用于设置建立连接的超时时间等
                val httpProfile = HttpProfile()
                httpProfile.endpoint = "tiia.tencentcloudapi.com"  // 服务地址
                httpProfile.connTimeout = 60
                httpProfile.readTimeout = 60
                // 实例化一个客户端配置对象，可以指定超时时间等配置
                val clientProfile = ClientProfile()
                clientProfile.httpProfile = httpProfile
                // 实例化要请求的客户端IIClient对象
                val client = TiiaClient(cred, "ap-shanghai", clientProfile)

                // 实例化一个请求对象, 传入请求参数
                val req = DetectLabelProRequest()

                req.setImageBase64(imageView.convertToBase64()) // 图片的URL

                //                Log.d("Image",imageView.convertToBase64())
            var resp: DetectLabelProResponse = DetectLabelProResponse()
            val thread = Thread {
                try {
                    // 通过客户端对象调用相应的接口方法发送请求，并获取返回结果
                    resp = client.DetectLabelPro(req)

                    // 输出json格式的字符串回包
//                   Log.d("Result", DetectLabelProResponse.toJsonString(resp))

                } catch (e: TencentCloudSDKException) {
                    e.printStackTrace()
                }
            }

            thread.start() // 启动线程
            thread.join() // 等待线程结束

            // 线程结束后的代码
//            textView.setText()
            var raw_result = DetectLabelProResponse.toJsonString(resp)

            textView.setText(raw_result)

            val jsonTokener = JSONTokener(raw_result)
            val jsonObject = JSONObject(jsonTokener)
            val labels = jsonObject.getJSONArray("Labels")

            if (labels != null) {
                val api = TransApi("", "")
                var content = ""
                for (i in 0..labels.length()-1){
                    val jsonTokener2 = JSONTokener(labels[i].toString())
                    val jsonObject2 = JSONObject(jsonTokener2)
                    val name = jsonObject2.getString("Name")
                    val thread = Thread{
                        val result = api.getTransResult(name, "auto", "en")

                        val jsonTokener3 = JSONTokener(result)
                        val jsonObject3 = JSONObject(jsonTokener3)
                        val names = jsonObject3.getJSONArray("trans_result")

                        val jsonTokener4 = JSONTokener(names[0].toString())
                        val jsonObject4 = JSONObject(jsonTokener4)

                        val name_en = jsonObject4.getString("dst")

//                        Log.d("结果", name+": " + name_en)
                        content+=name+": " + name_en + "\n"
                    }

                    thread.start() // 启动线程
                    thread.join() // 等待线程结束

                    textView.setText(content)

                }
            }

        }
    }

    fun ImageView.convertToBase64(): String {
        val drawable = this.drawable
        return if (drawable is BitmapDrawable) {
            drawable.bitmap.convertToBase64()
        } else {
            // 如果ImageView不是BitmapDrawable，则需要将其转换为Bitmap
            val bitmap = (drawable as? VectorDrawable)?.let { dr ->
                Bitmap.createBitmap(dr.intrinsicWidth, dr.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
                    val canvas = Canvas(this)
                    dr.setBounds(0, 0, canvas.width, canvas.height)
                    dr.draw(canvas)
                }
            } ?: throw IllegalArgumentException("Drawable cannot be converted to Bitmap")
            bitmap.convertToBase64()
        }
    }

    fun Bitmap.convertToBase64(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream) // 这里选择了PNG格式进行压缩，也可以根据需要更改

        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver
        .openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 将拍摄的照片显示出来
                    val bitmap = BitmapFactory.decodeStream(contentResolver.
                    openInputStream(imageUri))
                    imageView.setImageBitmap(rotateIfRequired(bitmap))
                    textView.setText("ready")

                    saveImageToGallery(this, rotateIfRequired(bitmap), "")
                }
            }
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的图片显示
                        val bitmap = getBitmapFromUri(uri)
                        imageView.setImageBitmap(bitmap)
                        textView.setText("ready")
                    }
                }
            }
        }
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap, imageName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyAppName")
        }

        val imageUri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream: OutputStream? ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }

        return imageUri
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true)
        bitmap.recycle() // 将不再需要的Bitmap对象回收
        return rotatedBitmap
    }
}