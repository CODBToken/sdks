package com.codb.sdk

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test

@DisplayName("test api")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Test {

    lateinit var api: Api

    @BeforeAll
    fun testBefore() {
        api = WalletSDK.create("21444693860123649", "CBrfEyXqoCzbhefryT3SJDibk3LO6R22", "xXmuje09VreuP3hB")
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

}