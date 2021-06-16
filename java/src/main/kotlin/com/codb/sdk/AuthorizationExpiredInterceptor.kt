package com.codb.sdk

import com.codb.sdk.model.Platform
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory

internal class AuthorizationExpiredInterceptor : Interceptor {

    private val logger = LoggerFactory.getLogger(AuthorizationExpiredInterceptor::class.java)
    private val authPath = "/api/platform/token"
    private var token: String = ""
    var apiService: ApiService? = null
    var platform: Platform? = null
    private var refreshTime = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        var origin = chain.request()
        if (origin.url().uri().path == authPath) {
            return chain.proceed(origin)
        }
        try {
            origin = if (token.isEmpty()) {
                val builder = origin.newBuilder()
                updateAuthorization(chain, builder)
                builder.build()
            } else {
                origin.newBuilder().header("token", token).build()
            }
            var response = chain.proceed(origin)
            if (isExpired(response)) {
                logger.warn("token expired")
                val expiredRequestBuilder = origin.newBuilder()
                logger.info("refresh token")
                if (updateAuthorization(chain, expiredRequestBuilder)) {
                    logger.info("token refresh success")
                    response = chain.proceed(expiredRequestBuilder.build())
                } else {
                    logger.error("token refresh fail")
                }
            }
            return response
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chain.proceed(origin)
    }

    /**
     * 判断请求返回内容是否标记 token 过期
     */
    private fun isExpired(response: Response): Boolean {
        val code = response.code()
        return code == 401 || code == 403
    }

    /**
     * 更新token
     */
    @Synchronized
    private fun updateAuthorization(chain: Interceptor.Chain, expiredRequestBuilder: Request.Builder): Boolean {
        val time = System.currentTimeMillis() - refreshTime
        if (time <= 60000) {
            expiredRequestBuilder.header("token", token)
            return true
        }
        logger.warn("刷新token")
        val response = apiService?.getToken(mapOf("no" to platform?.no, "accessKey" to platform?.accessKey))?.execute()?.body()
        if (response != null && response.isSuccessful()) {
            val data = response.data
            if (data != null) {
                expiredRequestBuilder.header("token", data.token)
                this.token = data.token
                refreshTime = System.currentTimeMillis()
                return true
            }
        }
        return false
    }
}