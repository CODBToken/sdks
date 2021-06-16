package com.codb.sdk

import com.codb.sdk.model.ApiResponse
import com.codb.sdk.model.Asset
import com.codb.sdk.model.Platform
import com.codb.sdk.model.Token
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

internal interface ApiService {

    @POST("platform/token")
    fun getToken(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any?>): Call<ApiResponse<Token>>

    @POST("platform/publish")
    fun publish(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any?>): Call<ApiResponse<Any?>>

    @POST("platform/asset/transfer")
    fun transfer(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any?>): Call<ApiResponse<Any?>>

    @POST("platform/registerUser")
    fun registerUser(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any?>): Call<ApiResponse<String>>

    @POST("platform/asset/updateUserAssetStatus")
    fun updateWalletAssetStatus(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any?>): Call<ApiResponse<Any?>>

    @GET("platform/info")
    fun getPlatformInfo(): Call<ApiResponse<Platform>>

    @GET("platform/platformUserInfo")
    fun getPlatformUserInfo(@Query("uid") uid: String): Call<ApiResponse<Any?>>

    @GET("platform/asset/listPlatformAssets")
    fun getPlatformAssets(): Call<ApiResponse<List<Asset>>>

    @GET("platform/asset/userAssetLog")
    fun getWalletAssetLog(@Query("address") address: String, @Query("contract") contract: String?, @Query("page") page: Int, @Query("limit") limit: Int): Call<ApiResponse<List<Asset>>>

    @GET("platform/asset/listUserAssets")
    fun getWalletAssets(@Query("address") address: String): Call<ApiResponse<List<Asset>>>

    @GET("platform/asset/userAsset")
    fun getWalletAsset(
        @Query("address") address: String,
        @Query("contract") contract: String?
    ): Call<ApiResponse<Asset>>

    @GET("platform/asset/info")
    fun getAssetInfo(@Query("contract") contract: String): Call<ApiResponse<Asset>>
}