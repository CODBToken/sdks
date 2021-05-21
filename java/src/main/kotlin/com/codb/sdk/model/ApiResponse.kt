package com.codb.sdk.model

class ApiResponse<T> {
    var code: Int = -1
    var data: T? = null
    var message: String? = null

    fun isSuccessful() = code == 200

    companion object{
        fun getSuccessCode() = 200
    }
}