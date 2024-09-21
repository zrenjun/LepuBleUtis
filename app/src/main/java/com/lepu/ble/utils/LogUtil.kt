package com.lepu.ble.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import io.getstream.log.StreamLog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*


/**
 * Created by zrj 2017/6/10.
 */
@SuppressLint("StaticFieldLeak")
object LogUtil {
    private var logEnabled = true
    private var tag = "zrj"
    private var isSaveLog = true
    private lateinit var context: Context

    fun init(context: Context) {
        LogUtil.context = context
    }

    fun v(msg: String, customTag: String = tag) {
        log(customTag, msg)
    }

    //网络有关的日志
    fun d(msg: String, customTag: String = tag) {
        log(customTag, msg)
    }

    fun i(msg: String, customTag: String = tag) {
        log(customTag, msg)
    }

    fun ble(msg: String, customTag: String = tag) {
        if (!logEnabled) return
        val elements = Thread.currentThread().stackTrace
        val index = findIndex(elements)
        val element = elements[index]
        val tag = handleTag(element, customTag)
        Log.println(Log.INFO, tag, msg)
        if (isSaveLog) {
            point(tag, msg)
        }
    }

    @Suppress("unused")
    fun w(msg: String, customTag: String = tag) {
        log(customTag, msg)
    }

    fun e(msg: String, customTag: String = tag) {
        log(customTag, msg)
    }

    fun e(msg: Int, customTag: String = tag) {
        log(customTag, "$msg")
    }

    fun e(msg: Float, customTag: String = tag) {
        log(customTag, "$msg")
    }

    fun e(msg: Long, customTag: String = tag) {
        log(customTag, "$msg")
    }

    fun json(msg: String, customTag: String = tag) {
        val json = formatJson(msg)
        log(customTag, json)
    }

    /**
     * 格式化json
     */
    private fun formatJson(json: String): String {
        return try {
            val trimJson = json.trim()
            when {
                trimJson.startsWith("{") -> JSONObject(trimJson).toString(4)
                trimJson.startsWith("[") -> JSONArray(trimJson).toString(4)
                else -> trimJson
            }
        } catch (e: JSONException) {
            e.printStackTrace().toString()
        }
    }

    /**
     * 输出日志
     */
    private fun log(customTag: String, msg: String) {
        if (!logEnabled) return
        val elements = Thread.currentThread().stackTrace
        val index = findIndex(elements)
        val element = elements[index]
        val tag = handleTag(element, customTag)
        val content = "(${element.fileName}:${element.lineNumber}).${element.methodName}:  $msg"
        if (isSaveLog) {
            point(tag, content)
        }
    }


    /**
     * 处理tag逻辑
     */
    private fun handleTag(element: StackTraceElement, customTag: String): String = when {
        customTag.isNotBlank() -> customTag
        else -> element.className.substringAfterLast(".")
    }

    /**
     * 寻找当前调用类在[elements]中的下标
     */
    private fun findIndex(elements: Array<StackTraceElement>): Int {
        var index = 5
        while (index < elements.size) {
            val className = elements[index].className
            if (className != LogUtil::class.java.name && !elements[index].methodName.startsWith("log")) {
                return index
            }
            index++
        }
        return -1
    }

    private fun point(tag: String, msg: String) {
        try {
            if (isSaveLog) {
                val log = File("${context.getExternalFilesDir(null)?.path}")
                val dir = log.listFiles()
                if (dir != null && (dir.size > 7 || log.length() > 100 * 1024 * 1024)) {//5*4 5天  40M
                    //文件修改日期：递增
                    Arrays.sort(dir, object : Comparator<File> {
                        override fun compare(f1: File, f2: File): Int {
                            val diff = f1.lastModified() - f2.lastModified()
                            return if (diff > 0) 1 else if (diff == 0L) 0 else -1 //如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
                        }

                        override fun equals(other: Any?): Boolean {
                            return true
                        }
                    })
                    dir[0].delete()
                }
                StreamLog.e(tag = tag) { msg }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}


//分区存储空间
//val file = File(context.filesDir, filename)
//应用专属外部存储空间
//val appSpecificExternalDir = File(context.getExternalFilesDir(), filename)
//访问公共媒体目录文件
//val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, "${MediaStore.MediaColumns.DATE_ADDED} desc")
//if (cursor != null) {
//    while (cursor.moveToNext()) {
//        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
//        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
//        println("image uri is $uri")
//    }
//    cursor.close()
//}
//SAF (存储访问框架--Storage Access Framework)
//val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//intent.addCategory(Intent.CATEGORY_OPENABLE)
//intent.type = "image/*"
//startActivityForResult(intent, 100)
//
//@RequiresApi(Build.VERSION_CODES.KITKAT)
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//    if (data == null || resultCode != Activity.RESULT_OK)
//        return
//    if (requestCode == 100) {
//        val uri = data.data
//        println("image uri is $uri")
//    }
//}
//所有文件访问权限
//<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
//val intent = Intent()
//intent.action= Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//startActivity(intent)
//判断是否获取MANAGE_EXTERNAL_STORAGE权限：
//val isHasStoragePermission= Environment.isExternalStorageManager()