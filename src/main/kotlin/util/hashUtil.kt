package com.example.javac.incremental.util

import java.io.File
import java.security.MessageDigest
import java.util.*

internal val File.md5: String
    get() {
        val messageDigest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(4048)
        inputStream().use { input ->
            while (true) {
                val len = input.read(buffer)
                if (len < 0) {
                    break
                }
                messageDigest.update(buffer, 0, len)
            }
        }
        return Base64.getEncoder().encodeToString(messageDigest.digest())
    }