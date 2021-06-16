package com.codb.sdk

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test

@DisplayName("test api")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Test {

    lateinit var api: Api

    @BeforeAll
    fun testBefore() {
        api = WalletSDK.create("", "", "")
    }

    @Test
    fun getPlatformInfo() {
        Assertions.assertTrue(api.getPlatformInfo()?.isSuccessful() ?: false)
    }

    @Test
    fun getPlatformAssets() {
        Assertions.assertTrue(api.getPlatformAssets()?.isSuccessful() ?: false)
    }

    @Test
    fun getAssetInfo() {
        Assertions.assertTrue(api.getAssetInfo("0x5f26fc8ad453fd18694840b6ae04383a08ce7fbe")?.isSuccessful() ?: false)
    }

    @Test
    fun getWalletAssets() {
        Assertions.assertTrue(api.getWalletAssets("0x5f26fc8ad453fd18694840b6ae04383a08ce7fbe")?.isSuccessful() ?: false)
    }

    @Test
    fun getPlatformUserInfo(){
        val result = api.getPlatformUserInfo("oapfJ5aJY_iWpl_mo3ZP0eH3-XSI")
        println(result?.data)
    }

}