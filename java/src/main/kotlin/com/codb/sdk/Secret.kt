package com.codb.sdk

import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

internal object Secret {
    @JvmStatic
    fun encrypt(key: ByteArray, text: String): String {
        val secretKey = SecretKeySpec(to16ByteArray(key), "AES")
        val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return String(Base64.getEncoder().encode(cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))), StandardCharsets.UTF_8)
    }

    @JvmStatic
    fun decrypt(key: ByteArray, text: String): String {
        val secretKey = SecretKeySpec(to16ByteArray(key), "AES")
        val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(Base64.getDecoder().decode(text.toByteArray(StandardCharsets.UTF_8))), StandardCharsets.UTF_8)
    }

    @JvmStatic
    private fun to16ByteArray(array: ByteArray): ByteArray {
        var len = array.size
        if (len == 16) return array
        if (len > 16) {
            len = 16
        }
        return array.copyInto(ByteArray(16), 0, 0, len)
    }
}