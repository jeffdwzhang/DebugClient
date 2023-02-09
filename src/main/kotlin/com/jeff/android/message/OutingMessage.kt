package com.jeff.android.message

import com.alibaba.fastjson2.JSONException
import com.alibaba.fastjson2.JSONObject

class OutingMessage(
    private val mMsgNo: Int,
    private val mMsgMethod: String
) {

    private var mContent: String? = null
    fun setMsgContent(content: String) {

        mContent = content
    }

    fun getMessageBody(): String {

        var jsonObject = JSONObject()
        try {
            jsonObject.put("msg_no", mMsgNo.toString())
            jsonObject.put("method", mMsgMethod)
            mContent?.let {
                if (it.isNotEmpty()) {
                    jsonObject.put("content", it)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }
}