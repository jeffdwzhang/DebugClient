package com.jeff.android.message

class ReceiveMsgHandler {

    companion object {
        private const val TAG = "ReceiveMsgHandler"
    }

    private var mTotalCount: Long = 0

    private var mMsgListener: OnMessageReceiveListener? = null

    private var mMessageHeader: ByteMessage.MessageHeader = ByteMessage.MessageHeader()

    private var mLastByteMessage: ByteMessage? = null

    fun setOnMessageReceiveListener(listener: OnMessageReceiveListener) {
        mMsgListener = listener
    }


    fun receivePacketData(data: ByteArray, length: Int) {
        if (length <= 0) {
            return
        }

        mTotalCount += length

        var currProcessIndex = 0

        mLastByteMessage?.let {
            var cnt = Math.min(length, it.getNeedCount())
            it.appendData(data, 0, cnt)
            currProcessIndex = cnt

            if (it.isComplete()) {
                mMsgListener?.onMessageReceive(it)
                mLastByteMessage = null
            }
        }

        if (currProcessIndex >= length) {
            // 所有数据都已经处理完了
            return
        }

        do {

            if (!mMessageHeader.isComplete()) {
                var cnt = Math.min(length - currProcessIndex, mMessageHeader.needCount())
                mMessageHeader.acceptData(data, currProcessIndex, cnt)
                currProcessIndex += cnt
            }

            if (mMessageHeader.isComplete()) {
                mLastByteMessage = ByteMessage.generateNewMsg(mMessageHeader.getHeaderData())
                mMessageHeader.reset()
            } else {
                // 剩余的数据都不足消息头，那就直接返回吧
                break
            }

            mLastByteMessage?.let {
                var cnt = Math.min(length - currProcessIndex, it.getNeedCount())
                it.appendData(data, currProcessIndex, cnt)
                currProcessIndex += cnt

                if (it.isComplete()) {
                    mMsgListener?.onMessageReceive(it)
                    mLastByteMessage = null
                }
            }

            if (currProcessIndex >= length) {
                // 所有数据都已经处理完了
                break
            }

        } while (true)

    }

}