package com.codb.sdk.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.*

annotation class NoArgOpenDataClass

@NoArgOpenDataClass
data class Platform(
    val no: String,
    val accessKey: String,
    val secretKey: String,
) {
    var name: String = ""
    var wallet: String = ""
    var status: Int? = null
    var transferRate: BigDecimal? = null
    var updateTime: Date? = null
    var createTime: Date? = null
}

@NoArgOpenDataClass
data class Token(
    var token: String,
    var expires: Long,
    @SerializedName("expire_date_time")
    var expireDateTime: Date
)

@NoArgOpenDataClass
data class Asset(
    var symbol: String,
    var contract: String,
    var name: String,
    var logo: String,
    var number: BigDecimal,
    var total: BigDecimal,
    var freezeNumber: BigDecimal,
    var introduce: String,
    var whiteBook: String,
    var status: Int,
    var updateTime: Date,
    var createTime: Date,

    var from: String,
    var to: String,
    var hash: String,
    var remark: String,
)