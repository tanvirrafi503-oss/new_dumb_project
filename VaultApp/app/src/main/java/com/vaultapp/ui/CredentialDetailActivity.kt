package com.vaultapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vaultapp.data.Credential
import com.vaultapp.databinding.ActivityCredentialDetailBinding
import kotlinx.coroutines.launch

class CredentialDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCredentialDetailBinding
    private val vm: CredentialViewModel by viewModels()
    private var credential: Credential? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCredentialDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getLongExtra("credential_id", -1L)
        lifecycleScope.launch {
            credential = vm.getById(id)
            credential?.let { display(it) }
        }

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, EditCredentialActivity::class.java)
                .putExtra("credential_id", id))
        }

        binding.btnCopyUsername.setOnClickListener {
            copyToClipboard("Username", credential?.username ?: "")
        }

        binding.btnCopyPassword.setOnClickListener {
            copyToClipboard("Password", credential?.password ?: "")
        }

        // Toggle password visibility
        var passwordVisible = false
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.tvPassword.text = if (passwordVisible)
                credential?.password else "••••••••"
            binding.btnTogglePassword.text = if (passwordVisible) "Hide" else "Show"
        }
    }

    private fun display(cred: Credential) {
        supportActionBar?.title = cred.appName
        binding.tvAppName.text = cred.appName
        binding.tvUsername.text = cred.username
        binding.tvPassword.text = "••••••••"
        binding.tvUrl.text = cred.url.ifBlank { "—" }
        binding.tvExtraInfo.text = cred.extraInfo.ifBlank { "—" }
        binding.tvAutoSaved.text = if (cred.isAutoSaved) "Auto-saved" else "Manual"
    }

    private fun copyToClipboard(label: String, text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, "$label copied!", Toast.LENGTH_SHORT).show()
    }
}
