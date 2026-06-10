package com.vaultapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedFile
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [Credential::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun credentialDao(): CredentialDao

    companion object {
        @Volatile private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                // Use Room with a regular (non-SQLCipher) build for simplicity.
                // Password-based encryption is handled at the app layer via CryptoManager.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
