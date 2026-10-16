package com.example.fooddelivery.data.websocket

import android.util.Log
import com.example.fooddelivery.BuildConfig
import com.example.fooddelivery.data.auth.TokenManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class OrderTrackingUpdate(
    @SerialName("delivery_status") val deliveryStatus: String,
    @SerialName("driver_name") val driverName: String? = null,
    @SerialName("estimated_time") val estimatedTime: Int? = null,
    @SerialName("driver_latitude") val driverLatitude: Double? = null,
    @SerialName("driver_longitude") val driverLongitude: Double? = null
)

@Singleton
class OrderWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager
) {
    private var webSocket: WebSocket? = null

    private val _orderUpdates = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val orderUpdates: SharedFlow<String> = _orderUpdates.asSharedFlow()

    fun connect(orderId: String) {
        disconnect()
        val token = tokenManager.getToken() ?: ""
        // WS /api/orders/{order_id}/ws?token={JWT}
        val baseUrl = BuildConfig.WS_BASE_URL.removeSuffix("/")
        val url = "$baseUrl/api/orders/$orderId/ws?token=$token"

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("OrderWebSocket", "WebSocket Connected for order $orderId")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("OrderWebSocket", "Received message: $text")
                _orderUpdates.tryEmit(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("OrderWebSocket", "WebSocket Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("OrderWebSocket", "WebSocket Failure", t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }
}
