package com.vaultapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vaultapp.databinding.ActivitySetupBinding
import com.vaultapp.security.CryptoManager

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateVault.setOnClickListener {
            val pwd = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            when {
                pwd.length < 6 -> Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                pwd != confirm -> Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                else -> {
                    CryptoManager.setupMasterPassword(this, pwd)
                    Toast.makeText(this, "Vault created!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
