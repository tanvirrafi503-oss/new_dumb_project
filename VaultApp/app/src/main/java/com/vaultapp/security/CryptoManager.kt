package com.vaultapp.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "VaultMasterKey"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val PREFS_FILE = "vault_secure_prefs"
    private const val PREF_MASTER_HASH = "master_hash"
    private const val PREF_MASTER_SALT = "master_salt"

    // ── KeyStore key for encrypting credentials ──────────────────────────────

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGen.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    // ── Master password hashing ───────────────────────────────────────────────

    private fun generateSalt(): String {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.DEFAULT)
        val input = saltBytes + password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }

    fun setupMasterPassword(context: Context, password: String) {
        val prefs = getSecurePrefs(context)
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        prefs.edit()
            .putString(PREF_MASTER_SALT, salt)
            .putString(PREF_MASTER_HASH, hash)
            .apply()
    }

    fun verifyMasterPassword(context: Context, password: String): Boolean {
        val prefs = getSecurePrefs(context)
        val salt = prefs.getString(PREF_MASTER_SALT, null) ?: return false
        val storedHash = prefs.getString(PREF_MASTER_HASH, null) ?: return false
        return hashPassword(password, salt) == storedHash
    }

    fun isMasterPasswordSet(context: Context): Boolean {
        val prefs = getSecurePrefs(context)
        return prefs.getString(PREF_MASTER_HASH, null) != null
    }

    private fun getSecurePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
