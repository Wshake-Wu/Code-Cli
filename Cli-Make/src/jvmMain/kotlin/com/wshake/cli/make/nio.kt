package com.wshake.cli.make

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
class NonBlockingInputStream(
    private val inputStream: InputStream,
    private val coroutineContext: CoroutineContext = Dispatchers.IO) : InputStream() {
    private val channel = Channel<Int>(Channel.UNLIMITED)

    init {
        // 启动一个协程来将输入流的数据写入通道
        CoroutineScope(coroutineContext).launch {
            try {
                while (true) {

                    val byte = inputStream.read()
                    if (byte == -1) break // 到达流的末尾
                    channel.send(byte)
                }
            } catch (e: IOException) {
                // 处理读取异常
                e.printStackTrace()
            } finally {
                channel.close()
            }
        }
    }
    // 覆盖 InputStream 的 read 方法
    override fun read(): Int {
        return runBlocking {
            select {
                channel.onReceiveCatching { it.getOrNull() ?: -1 }
            }
        }
    }


}