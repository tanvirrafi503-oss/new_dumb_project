package com.vaultapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials ORDER BY updatedAt DESC")
    fun getAllCredentials(): LiveData<List<Credential>>

    @Query("SELECT * FROM credentials WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Credential?

    @Query("SELECT * FROM credentials WHERE packageName = :pkg ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByPackage(pkg: String): Credential?

    @Query("SELECT * FROM credentials WHERE appName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): LiveData<List<Credential>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credential: Credential): Long

    @Update
    suspend fun update(credential: Credential)

    @Delete
    suspend fun delete(credential: Credential)

    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun deleteById(id: Long)
}
