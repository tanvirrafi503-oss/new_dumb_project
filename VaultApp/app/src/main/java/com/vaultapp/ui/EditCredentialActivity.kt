package com.vaultapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vaultapp.data.Credential
import com.vaultapp.databinding.ActivityEditCredentialBinding
import kotlinx.coroutines.launch

class EditCredentialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCredentialBinding
    private val vm: CredentialViewModel by viewModels()
    private var existingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCredentialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingId = intent.getLongExtra("credential_id", -1L)
        val prefillUsername = intent.getStringExtra("prefill_username") ?: ""
        val prefillPassword = intent.getStringExtra("prefill_password") ?: ""
        val prefillApp = intent.getStringExtra("prefill_app") ?: ""
        val prefillPkg = intent.getStringExtra("prefill_package") ?: ""

        if (existingId != -1L) {
            supportActionBar?.title = "Edit Credential"
            lifecycleScope.launch {
                vm.getById(existingId)?.let { cred ->
                    binding.etAppName.setText(cred.appName)
                    binding.etUsername.setText(cred.username)
                    binding.etPassword.setText(cred.password)
                    binding.etUrl.setText(cred.url)
                    binding.etExtraInfo.setText(cred.extraInfo)
                }
            }
        } else {
            supportActionBar?.title = "Add Credential"
            binding.etAppName.setText(prefillApp)
            binding.etUsername.setText(prefillUsername)
            binding.etPassword.setText(prefillPassword)
        }

        binding.btnSave.setOnClickListener { save(prefillPkg) }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun save(pkg: String) {
        val appName = binding.etAppName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (appName.isBlank() || username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "App name, username and password are required", Toast.LENGTH_SHORT).show()
            return
        }

        val cred = Credential(
            id = if (existingId != -1L) existingId else 0,
            appName = appName,
            packageName = pkg,
            username = username,
            password = password,
            url = binding.etUrl.text.toString().trim(),
            extraInfo = binding.etExtraInfo.text.toString().trim()
        )

        if (existingId != -1L) vm.update(cred) else vm.insert(cred)
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
