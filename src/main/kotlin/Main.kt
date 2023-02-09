import com.jeff.android.DebugSocketClient

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    // 执行adb forward命令
    var cmdStr = "adb forward tcp:9095 tcp:9095"
//    var process = ProcessBuilder(cmdStr).start()

    var client = DebugSocketClient()

    client.startDebug()
}