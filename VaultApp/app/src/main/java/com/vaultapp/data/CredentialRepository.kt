package com.vaultapp.data

import android.content.Context
import com.vaultapp.security.CryptoManager

class CredentialRepository(context: Context) {

    private val dao = VaultDatabase.getInstance(context).credentialDao()

    val allCredentials = dao.getAllCredentials()

    fun search(query: String) = dao.search(query)

    suspend fun insert(credential: Credential): Long {
        val encrypted = credential.copy(
            password = CryptoManager.encrypt(credential.password)
        )
        return dao.insert(encrypted)
    }

    suspend fun update(credential: Credential) {
        val encrypted = credential.copy(
            password = CryptoManager.encrypt(credential.password),
            updatedAt = System.currentTimeMillis()
        )
        dao.update(encrypted)
    }

    suspend fun delete(credential: Credential) = dao.delete(credential)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getById(id: Long): Credential? {
        val raw = dao.getById(id) ?: return null
        return try {
            raw.copy(password = CryptoManager.decrypt(raw.password))
        } catch (e: Exception) { raw }
    }

    suspend fun getByPackage(pkg: String): Credential? {
        val raw = dao.getByPackage(pkg) ?: return null
        return try {
            raw.copy(password = CryptoManager.decrypt(raw.password))
        } catch (e: Exception) { raw }
    }
}
