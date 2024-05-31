/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
import kotlinx.coroutines.*
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class NonBlockingInputStream(private val inputStream: InputStream) : InputStream() {
    private val buffer = ByteBuffer.allocate(1024)
    private var position = 0
    private var limit = 0
    private var eof = false

    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)

    override fun read(): Int {
        if (position < limit || !eof) {
            return readBuffer() and 0xFF
        }
        return -1
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (position < limit || !eof) {
            val bytesRead = readBuffer(b, off, len)
            if (bytesRead == 0 && eof) {
                return -1
            }
            return bytesRead
        }
        return -1
    }

    private fun readBuffer(): Int {
        if (position < limit) {
            return buffer[position++].toInt()
        }
        fillBuffer()
        return if (limit > 0) {
            buffer[position++].toInt()
        } else {
            -1
        }
    }

    private fun readBuffer(b: ByteArray, off: Int, len: Int): Int {
        if (position < limit) {
            val bytesRead = minOf(len, limit - position)
            buffer.get(b, off, bytesRead)
            position += bytesRead
            return bytesRead
        }
        fillBuffer()
        return if (limit > 0) {
            val bytesRead = minOf(len, limit)
            buffer.get(b, off, bytesRead)
            position += bytesRead
            bytesRead
        } else {
            0
        }
    }

    private fun fillBuffer() {
        buffer.clear()
        coroutineScope.launch {
            val bytesRead = withContext(Dispatchers.IO) {
                inputStream.read(buffer.array())
            }
            if (bytesRead == -1) {
                eof = true
            } else {
                limit = bytesRead
            }
        }.invokeOnCompletion {
            if (it != null) {
                // Handle any exception that occurred during non-blocking read
                it.printStackTrace()
            }
        }

        runBlocking {
            coroutineScope.coroutineContext[Job]?.join()
        }

        buffer.flip()
        position = 0
    }

    override fun close() {
        super.close()
        coroutineScope.cancel()
        inputStream.close()
    }
}