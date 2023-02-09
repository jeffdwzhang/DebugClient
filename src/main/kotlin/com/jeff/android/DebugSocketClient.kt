package com.jeff.android

import com.jeff.android.message.ByteMessage
import com.jeff.android.message.MsgType
import com.jeff.android.message.OnMessageReceiveListener
import com.jeff.android.message.ReceiveMsgHandler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.Socket
import java.util.Scanner
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Pattern

class DebugSocketClient: OnMessageReceiveListener {

    companion object {
        private const val SERVER_PORT = 9059
        private const val SERVER_IP = "127.0.0.1"

        private val mSendMsgNo = AtomicInteger(0)

        private val INPUT_PATTERN = Pattern.compile("[0-9]+")
    }

    private var mClientSocket: Socket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    private var mInputThread: Thread? = null
    private var mOutputThread: Thread? = null

    private var mReceiveMsgHandler: ReceiveMsgHandler? = null

    private val mLock: ReentrantLock = ReentrantLock()
    private val mCondition: Condition = mLock.newCondition()

    private val mSendServer: ExecutorService = ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())

    private var isConnected = false

    fun startDebug() {

        // 链接到Server
        tryToConnectServer()

        if (isConnected) {
            startSocketInputMonitor()
            startConsoleMonitor()
            //
            tryWait()
        }
        println("quit debug")
    }

    private fun tryWait() {
        mLock.lock()
        try {
            mCondition.await()
        } catch (e: InterruptedException ) {

        } finally {
            mLock.unlock()
        }
    }

    private fun tryNotify() {
        mLock.lock()
        mCondition.signal()
        mLock.unlock()
    }

    /**
     * 链接到服务器
     */
    private fun tryToConnectServer() {

        try {

            mClientSocket = Socket(SERVER_IP, SERVER_PORT)
            mClientSocket?.keepAlive = true
            mClientSocket?.soTimeout = 60 * 1000

            // 获取socket的输入输出流
            mInputStream = mClientSocket?.getInputStream()
            mOutputStream = mClientSocket?.getOutputStream()

            isConnected = true

            sendPlainTxtMsg("Hello, I'm Jeff!")

        } catch (e: Exception) {
            println("Connection failed!, e:${e}")
        }
    }

    private fun startSocketInputMonitor() {

        Thread {

            mInputStream?.let {

                mReceiveMsgHandler = ReceiveMsgHandler()
                mReceiveMsgHandler?.setOnMessageReceiveListener(this)

                val inputBuffer = ByteArray(1024)
                while (true) {
                    if (it.available() > 0) {
                        // 读取数据
                        var readCount = it.read(inputBuffer, 0, inputBuffer.size)
                        println("read from socket:$readCount")

                        // 处理读取到的数据
                        if (readCount > 0) {
                            mReceiveMsgHandler?.receivePacketData(inputBuffer, readCount)
                        }
                    } else {
                        Thread.sleep(400)
                    }

                    if (mClientSocket?.isInputShutdown == true) {
                        // 如果socket关闭了输入流，则退出
                        break
                    }
                }
            }

            // 唤醒其他线程
            tryNotify()

        }.start()
    }

    /**
     * 发送消息到服务器
     */
    private fun sendMsgToServer(msgType: Int, data: ByteArray) {
        mOutputStream?.let {

            if (!mSendServer.isShutdown()) {
                mSendServer.submit {
                    try {
                        var header = ByteMessage.generateMsgHeader(msgType, data.size)
                        it.write(header)
                        it.write(data)
                        it.flush()
                    } catch (e: IOException) {
                        // 发送数据失败，说明socket已经断开链接了
                    }
                }
            }

        }
    }

    /**
     * 目前是从控制台获取输入
     */
    fun startConsoleMonitor() {

        Thread {
            var scanner = Scanner(System.`in`, Charsets.UTF_8)

            while (true) {

                try {
                    if (scanner.hasNext()) {
                        var inputCmd = scanner.nextLine()
                        // 简单处理输入，判断输入是否合法
                        var matcher = INPUT_PATTERN.matcher(inputCmd)
                        if (inputCmd.isNotBlank() && matcher.matches()) {
                            val cmdNo = inputCmd.toInt()
                            when(cmdNo) {

                            }
                        } else {
                            println("illegal input:$inputCmd")
                        }

                    }
                } catch (e: Exception) {
                    break
                }
            }
        }.start()

    }

    private fun sendPlainTxtMsg(msg: String) {
        sendMsgToServer(MsgType.TYPE_PLAIN_TXT, msg.toByteArray())
    }

    override fun onMessageReceive(message: ByteMessage) {
        // 处理收到的消息
        when(message.getMsgType()) {
            MsgType.TYPE_PLAIN_TXT -> {
                println(ByteMessage.getTxtFromMsg(message))
            }
        }

        // 如果是非ack消息，返回一个ack消息，seq+1

    }
}