package com.codb.sdk

import com.codb.sdk.model.ApiResponse
import java.math.BigDecimal

class Api internal constructor(private val service: ApiService) {

    fun publish(symbol: String, name: String, total: BigDecimal, logo: String?, introduce: String, whiteBook: String?) = service.publish(
        mapOf(
            "symbol" to symbol,
            "name" to name,
            "total" to total,
            "logo" to logo,
            "introduce" to introduce,
            "whiteBook" to whiteBook
        )
    )

    fun transfer(contract: String, from: String, to: String, amount: BigDecimal, remark: String) = service.transfer(
        mapOf(
            "contract" to contract,
            "from" to from,
            "to" to to,
            "amount" to amount,
            "remark" to remark,
        )
    )

    fun updateWalletAssetStatus(wallet: String, contract: String, action: Int, amount: BigDecimal) = service.updateWalletAssetStatus(
        mapOf(
            "wallet" to wallet,
            "contract" to contract,
            "action" to action,
            "amount" to amount,
        )
    )

    fun registerUser(uid: String): ApiResponse<String>? {
        return service.registerUser(mapOf("uid" to uid)).execute().body()
    }

    fun getPlatformInfo() = service.getPlatformInfo().execute().body()

    fun getPlatformAssets() = service.getPlatformAssets().execute().body()

    fun getWalletAssets(address: String) = service.getWalletAssets(address).execute().body()

    fun getWalletAsset(address: String, contract: String) = service.getWalletAsset(address, contract).execute().body()

    fun getWalletAssetLog(address: String, contract: String, page: Int, limit: Int) = service.getWalletAssetLog(address, contract, page, limit).execute().body()

    fun getAssetInfo(contract: String) = service.getAssetInfo(contract).execute().body()

}