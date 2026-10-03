package com.yindun.shouhu.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密管理器
 * 使用Android Keystore和AES-256-GCM进行数据加密
 */
class CryptoManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_NAME = "AndroidKeyStore"
        private const val KEY_ALIAS = "YinDunShouHuKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16

        @Volatile
        private var instance: CryptoManager? = null

        fun getInstance(context: Context): CryptoManager {
            return instance ?: synchronized(this) {
                instance ?: CryptoManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    init {
        generateKeyIfNotExists()
    }

    /**
     * 生成密钥（如果不存在）
     */
    private fun generateKeyIfNotExists() {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) return

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_NAME
        )

        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenSpec)
        keyGenerator.generateKey()
    }

    /**
     * 获取密钥
     */
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * 加密字符串
     * @param plainText 明文
     * @return Base64编码的密文（包含IV）
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 将IV和密文组合
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * 解密字符串
     * @param encryptedText Base64编码的密文
     * @return 明文
     */
    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * 加密字节数组
     */
    fun encryptBytes(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)

        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return combined
    }

    /**
     * 解密字节数组
     */
    fun decryptBytes(encryptedData: ByteArray): ByteArray {
        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        return cipher.doFinal(encryptedBytes)
    }

    /**
     * 使用简单密钥加密（用于本地数据库字段）
     * 注意：这种方式密钥是硬编码的，仅用于本地存储的简单加密
     */
    fun simpleEncrypt(plainText: String): String {
        return try {
            val key = getOrCreateSimpleKey()
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            plainText // 加密失败时返回原文
        }
    }

    /**
     * 使用简单密钥解密
     */
    fun simpleDecrypt(encryptedText: String): String {
        return try {
            val key = getOrCreateSimpleKey()
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decrypted = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT))
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText // 解密失败时返回原文
        }
    }

    /**
     * 获取或创建简单加密密钥
     */
    private fun getOrCreateSimpleKey(): SecretKey {
        val prefs = context.getSharedPreferences("crypto_prefs", Context.MODE_PRIVATE)
        val existingKey = prefs.getString("simple_key", null)

        return if (existingKey != null) {
            val keyBytes = Base64.decode(existingKey, Base64.DEFAULT)
            SecretKeySpec(keyBytes, "AES")
        } else {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val secretKey = keyGenerator.generateKey()
            prefs.edit().putString("simple_key", Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)).apply()
            secretKey
        }
    }

    /**
     * 计算哈希值
     */
    fun hash(data: String): String {
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = messageDigest.digest(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * 生成随机盐值
     */
    fun generateSalt(): String {
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    /**
     * 删除密钥
     */
    fun deleteKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        keyStore.deleteEntry(KEY_ALIAS)
    }
}

/**
 * 数据加密扩展函数
 */
fun String.encrypt(cryptoManager: CryptoManager): String = cryptoManager.encrypt(this)
fun String.decrypt(cryptoManager: CryptoManager): String = cryptoManager.decrypt(this)
fun String.simpleEncrypt(cryptoManager: CryptoManager): String = cryptoManager.simpleEncrypt(this)
fun String.simpleDecrypt(cryptoManager: CryptoManager): String = cryptoManager.simpleDecrypt(this)
