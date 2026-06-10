package com.vaultapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vaultapp.databinding.ActivitySavePromptBinding
import com.vaultapp.service.VaultAccessibilityService

class SavePromptActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavePromptBinding
    private val vm: CredentialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavePromptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pkg = intent.getStringExtra(VaultAccessibilityService.EXTRA_PACKAGE) ?: ""
        val appName = intent.getStringExtra(VaultAccessibilityService.EXTRA_APP_NAME) ?: pkg
        val username = intent.getStringExtra(VaultAccessibilityService.EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(VaultAccessibilityService.EXTRA_PASSWORD) ?: ""

        binding.tvPromptTitle.text = "Save login for $appName?"
        binding.tvPromptUsername.text = username

        binding.btnSaveNow.setOnClickListener {
            startActivity(
                Intent(this, EditCredentialActivity::class.java).apply {
                    putExtra("prefill_app", appName)
                    putExtra("prefill_package", pkg)
                    putExtra("prefill_username", username)
                    putExtra("prefill_password", password)
                }
            )
            finish()
        }

        binding.btnDismiss.setOnClickListener { finish() }
    }
}
