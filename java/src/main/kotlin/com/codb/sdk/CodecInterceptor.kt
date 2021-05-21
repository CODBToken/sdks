package com.codb.sdk

import com.codb.sdk.model.ApiResponse
import com.codb.sdk.model.Platform
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

internal class CodecInterceptor : Interceptor {

    private val logger = LoggerFactory.getLogger(CodecInterceptor::class.java)
    var platform: Platform? = null
    private val authPath = "/api/platform/token"

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.url().uri().path == authPath) {
            return chain.proceed(request)
        }
        if (request.method().equals("POST", false)) {
            val body = request.body()
            val platform = this.platform
            if (body != null && platform != null) {
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                var charset = Charset.forName("UTF-8")
                val contentType = body.contentType()
                if (contentType != null) {
                    charset = contentType.charset(charset) ?: charset
                }
                val bodyString = buffer.readString(charset)
                val newData = Secret.encrypt(platform.secretKey.toByteArray(), bodyString)
                logger.info("request -> source: {}, encrypt: {}", bodyString, newData)
                request = request.newBuilder().post(RequestBody.create(contentType, newData)).build()
            }
        }
        var response = chain.proceed(request)
        val body = response.body()
        if (response.isSuccessful && body != null) {
            val source = body.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            var charset = Charset.forName("UTF-8")
            val contentType = body.contentType()
            if (contentType != null) {
                charset = contentType.charset(charset) ?: charset
            }
            val bodyString = buffer.clone().readString(charset)
            val obj = JSONObject(bodyString)
            val data = obj.optString("data")
            val code = obj.optInt("code", -1)
            val platform = this.platform
            if (data != null && platform != null && code == ApiResponse.getSuccessCode()) {
                val newData = Secret.decrypt(platform.secretKey.toByteArray(), data).trim()
                logger.info("response -> source: {}, decrypt: {}", data, newData)
                if (newData.startsWith("{")) {
                    obj.put("data", JSONObject(newData))
                } else if (newData.startsWith("[")) {
                    obj.put("data", JSONArray(newData))
                } else {
                    obj.put("data", newData)
                }
            }
            response = response.newBuilder().body(ResponseBody.create(contentType, obj.toString())).build()
        }
        return response
    }
}