package com.codb.sdk

import com.codb.sdk.model.ApiResponse
import java.math.BigDecimal

class Api internal constructor(private val service: ApiService) {

    /**
     * 发布资产
     * @param symbol 资产名（只能使用字母）
     * @param name 资产别名（可以用中文0
     * @param total 发布数量
     * @param logo 资产logo
     * @param introduce 资产介绍
     * @param whiteBook 白皮书
     * @return 调用远程接口是否成功
     */
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

    /**
     * 转帐
     * @param contract 资产合约地址
     * @param from 付款地址
     * @param to 收款地址
     * @param amount 转帐数量
     * @param remark 备注
     * @param gasContract 平台收费使用的资产
     * @param gasFee 费用
     * @return 调用远程接口是否成功
     */
    fun transfer(contract: String, from: String, to: String, amount: BigDecimal, remark: String,  gasContract: String?, gasFee: String?){
        val params = mutableMapOf<String, Any?>(
            "contract" to contract,
            "from" to from,
            "to" to to,
            "amount" to amount,
            "remark" to remark,
        )
        if(gasContract != null && gasFee != null){
            params["gas"] = mapOf<String, Any?>(
                "contract" to gasContract,
                "free" to gasFee,
            )
        }
        service.transfer(params)
    }

    /**
     * 操作指定钱包资产数量
     * @param wallet 指定的钱包地址
     * @param contract 指定的合约地址
     * @param action 0: 解除冻结, 1: 冻结
     * @param amount 解除冻结或者冻结数量
     * @return 调用远程接口是否成功
     */
    fun updateWalletAssetStatus(wallet: String, contract: String, action: Int, amount: BigDecimal) = service.updateWalletAssetStatus(
        mapOf(
            "wallet" to wallet,
            "contract" to contract,
            "action" to action,
            "amount" to amount,
        )
    )

    /**
     * 注册平台用户
     * @param uid 该平台用户唯一标识，不能重复
     * @return 该用户钱包地址
     */
    fun registerUser(uid: String): ApiResponse<String>? {
        return service.registerUser(mapOf("uid" to uid)).execute().body()
    }

    /**
     * 获取平台信息
     * @return 平台信息封装对象
     */
    fun getPlatformInfo() = service.getPlatformInfo().execute().body()

    /**
     * 获取平台资产列表
     * @return 平台资产列表
     */
    fun getPlatformAssets() = service.getPlatformAssets().execute().body()

    /**
     * 获取指定钱包持有资产列表
     * @param address 指定的钱包地址
     * @return 资产列表
     */
    fun getWalletAssets(address: String) = service.getWalletAssets(address).execute().body()

    /**
     * 获取指定钱包指定资产持有信息
     * @param address 指定的钱包地址
     * @param contract 指定的合约地址
     * @return 资产信息
     */
    fun getWalletAsset(address: String, contract: String) = service.getWalletAsset(address, contract).execute().body()

    /**
     * 获取指定钱包资产变动记录
     * @param address 要获取的钱包地址
     * @param contract 合约地址, 不传则是平台下的所有资产变动
     * @param page 页码，1开始
     * @param limit 每页数量
     * @return 资产变动列表
     */
    fun getWalletAssetLog(address: String, contract: String, page: Int, limit: Int) = service.getWalletAssetLog(address, contract, page, limit).execute().body()

    /**
     * 获取资产详情
     * @param contract 指定的合约地址
     * @return 资产详情
     */
    fun getAssetInfo(contract: String) = service.getAssetInfo(contract).execute().body()

}