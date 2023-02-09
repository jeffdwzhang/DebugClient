package com.jeff.android.message

class ByteMessage(
    private val mMsgType: Int,
    private val mMsgLength: Int
) {

    companion object {
        const val HEADER_LENGTH = 3

        fun getTxtFromMsg(message: ByteMessage): String? {
            if (message.mMsgType != MsgType.TYPE_PLAIN_TXT) {
                return null
            }
            if (!message.isComplete()) {
                return null
            }
            return String(message.getMsgData())
        }

        /**
         * 生成消息头
         */
        fun generateMsgHeader(msgType: Int, msgLength: Int): ByteArray {
            val header = ByteArray(HEADER_LENGTH)
            header[0] = MsgType.getByteOfType(msgType)
            header[1] = ((msgLength shr 8) and 0xff).toByte()
            header[2] = (msgLength and 0xff).toByte()
            return header
        }       

        fun getMsgLength(data: ByteArray): Int {
            return (((data[1].toInt() and 0xff) shl 8) or (data[2].toInt() and 0xff))
        }

        fun generateNewMsg(headerData: ByteArray): ByteMessage {
            var mstType = headerData[0].toInt()
            var length = getMsgLength(headerData)
            return ByteMessage(mstType, length)
        }
    }

    private val mMsgData = ByteArray(mMsgLength)

    private var mCurrIndex = 0

    fun getMsgType(): Int {
        return mMsgType
    }

    fun getMsgData(): ByteArray {
        return mMsgData
    }

    fun isComplete(): Boolean {
        return mCurrIndex == mMsgLength
    }

    fun getNeedCount() : Int {
        return mMsgLength - mCurrIndex
    }

    fun appendData(data: ByteArray, offset: Int, length: Int) {
        if (length <= 0 || (offset + length) > data.size) {
            // 参数不合法，忽略
            return
        }
        System.arraycopy(data, offset, mMsgData, mCurrIndex, length)
        mCurrIndex += length
    }

    class MessageHeader {

        private val mHeaderData: ByteArray = ByteArray(ByteMessage.HEADER_LENGTH)
        private var mFilledLength = 0

        fun acceptData(data:ByteArray, offset: Int, length: Int) {
            for (i in 0 until length) {
                mHeaderData[mFilledLength + i] = data[offset + i]
            }
            mFilledLength += length
        }

        fun isComplete(): Boolean {
            return mFilledLength == HEADER_LENGTH
        }

        fun needCount(): Int {
            return HEADER_LENGTH - mFilledLength
        }

        fun getHeaderData(): ByteArray {
            return mHeaderData
        }

        fun reset() {
            mHeaderData.fill(0, 0, mHeaderData.size)
            mFilledLength = 0
        }

    }
}