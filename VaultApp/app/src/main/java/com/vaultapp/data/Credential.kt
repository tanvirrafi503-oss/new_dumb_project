package com.vaultapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val appName: String = "",
    val packageName: String = "",   // auto-filled by accessibility service
    val username: String = "",
    val password: String = "",      // stored AES-encrypted
    val url: String = "",
    val extraInfo: String = "",     // any additional notes
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isAutoSaved: Boolean = false
)
