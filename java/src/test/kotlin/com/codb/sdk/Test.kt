package com.codb.sdk

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@DisplayName("test api")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Test {

    val threadSize = 20
    val taskSize = 150
    val threads = Executors.newFixedThreadPool(threadSize)
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

    @Test
    fun getPlatformUserInfo() {
        val result = api.getPlatformUserInfo("oapfJ5aJY_iWpl_mo3ZP0eH3-XSI")
        println(result?.data)
    }

    @Test
    fun yace() {
        val latch = CountDownLatch(threadSize)
        for (i in 1..threadSize) {
            threads.execute {
                for (j in 1..taskSize) {
                    val amount = String.format("%06f", (Math.random() * 10))
                    api.transfer("0xda42e5ce9ddc3621ae08f6b2821f3ac4187f1791", "0x5f26fc8ad453fd18694840b6ae04383a08ce7fbe", "0xead7584b80a768e95667cdac582bd2db61fcaad0", BigDecimal(amount), "压测数据2", null, null)
                }
                latch.countDown()
            }
        }
        latch.await()
    }

}