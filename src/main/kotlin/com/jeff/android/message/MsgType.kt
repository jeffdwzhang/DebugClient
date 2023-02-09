package com.jeff.android.message

class MsgType {

    companion object{
        const val TYPE_PLAIN_TXT = 1

        const val TYPE_JSON_STRING = 2

        const val TYPE_BLOB = 3

        fun getByteOfType(type: Int) : Byte {
            return (type and 0xff).toByte()
        }

        fun getType(type: Byte): Int {
            return type.toInt()
        }
    }
}