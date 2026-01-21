package com.aizhigu.armcontroller.connection

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * WiFi WebSocket 连接实现
 * 使用 OkHttp WebSocket 客户端
 */
class WifiConnectionImpl(
    private val host: String,
    private val port: Int = 81
) : DeviceConnection {
    
    override val deviceInfo: DeviceInfo = DeviceInfo(
        id = "$host:$port",
        name = "WiFi Device ($host)",
        type = ConnectionType.WIFI
    )
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private var onDataReceivedCallback: ((ByteArray) -> Unit)? = null
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = ConnectionState.CONNECTED
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            onDataReceivedCallback?.invoke(text.toByteArray(Charsets.UTF_8))
        }
        
        override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
            onDataReceivedCallback?.invoke(bytes.toByteArray())
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = ConnectionState.ERROR
        }
    }
    
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            
            val request = Request.Builder()
                .url("ws://$host:$port/ws")
                .build()
            
            webSocket = client.newWebSocket(request, webSocketListener)
            
            // 等待连接建立
            var retries = 50 // 5 秒超时
            while (_connectionState.value == ConnectionState.CONNECTING && retries > 0) {
                delay(100)
                retries--
            }
            
            _connectionState.value == ConnectionState.CONNECTED
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    override suspend fun disconnect() {
        webSocket?.close(1000, "User disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    override suspend fun sendCommand(command: String): Boolean {
        return try {
            webSocket?.send(command) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun sendJson(json: String): Boolean {
        return sendCommand(json)
    }
    
    override fun setOnDataReceived(callback: (ByteArray) -> Unit) {
        onDataReceivedCallback = callback
    }
    
    /**
     * 静态方法用于设备发现
     * 通过 IP 地址范围扫描
     */
    companion object {
        suspend fun scanLocalNetwork(
            baseIp: String = "192.168",
            subnet: Int = 1,
            port: Int = 81,
            timeout: Long = 200
        ): List<String> = withContext(Dispatchers.IO) {
            val foundDevices = mutableListOf<String>()
            
            // 扫描 1-254 范围
            (1..254).map { host ->
                async {
                    val ip = "$baseIp.$subnet.$host"
                    try {
                        val socket = java.net.Socket()
                        socket.connect(java.net.InetSocketAddress(ip, port), timeout.toInt())
                        socket.close()
                        ip
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull().also { foundDevices.addAll(it) }
            
            foundDevices
        }
    }
}
