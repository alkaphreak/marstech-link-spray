package fr.marstech.e2e

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory S3 stand-in (PUT, GET, DELETE on objects) on the JDK HTTP server, so the image routes run
 * their success path without DigitalOcean Spaces (MLS-277). Objects are keyed by request path, which
 * covers both path-style and virtual-host addressing on a single host.
 */
object FakeS3 {
    private val objects = ConcurrentHashMap<String, Pair<String, ByteArray>>()

    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange -> exchange.use { handle(it) } }
        start()
    }

    val endpoint: String get() = "http://127.0.0.1:${server.address.port}"

    private fun handle(exchange: HttpExchange) {
        val key = exchange.requestURI.path
        when (exchange.requestMethod) {
            "PUT" -> {
                val raw = exchange.requestBody.readAllBytes()
                val chunked = exchange.requestHeaders.getFirst("Content-Encoding")?.contains("aws-chunked") == true ||
                    exchange.requestHeaders.getFirst("x-amz-content-sha256")?.startsWith("STREAMING") == true
                val body = if (chunked) decodeAwsChunked(raw.inputStream()) else raw
                objects[key] = (exchange.requestHeaders.getFirst("Content-Type") ?: "application/octet-stream") to body
                exchange.responseHeaders.add("ETag", "\"e2e\"")
                exchange.sendResponseHeaders(200, -1)
            }
            "GET" -> {
                val stored = objects[key]
                if (stored == null) {
                    exchange.sendResponseHeaders(404, -1)
                } else {
                    exchange.responseHeaders.add("Content-Type", stored.first)
                    exchange.sendResponseHeaders(200, stored.second.size.toLong())
                    exchange.responseBody.write(stored.second)
                }
            }
            "DELETE" -> {
                objects.remove(key)
                exchange.sendResponseHeaders(204, -1)
            }
            else -> exchange.sendResponseHeaders(405, -1)
        }
    }

    /** `aws-chunked`: `<hex size>[;ext]\r\n<data>\r\n` repeated, ended by a 0-size chunk and optional trailers. */
    private fun decodeAwsChunked(input: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        while (true) {
            val size = readLine(input).substringBefore(';').trim().toInt(16)
            if (size == 0) return out.toByteArray()
            out.write(input.readNBytes(size))
            readLine(input)
        }
    }

    private fun readLine(input: InputStream): String {
        val line = StringBuilder()
        while (true) {
            val c = input.read()
            if (c == -1 || c == '\n'.code) return line.toString().trimEnd('\r')
            line.append(c.toChar())
        }
    }
}
