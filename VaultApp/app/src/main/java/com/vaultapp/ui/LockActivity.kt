package com.vaultapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vaultapp.databinding.ActivityLockBinding
import com.vaultapp.security.BiometricHelper
import com.vaultapp.security.CryptoManager

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // First run → go to setup
        if (!CryptoManager.isMasterPasswordSet(this)) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        setupUI()
        tryBiometric()
    }

    private fun setupUI() {
        binding.btnUnlockFingerprint.visibility =
            if (BiometricHelper.isBiometricAvailable(this)) View.VISIBLE else View.GONE

        binding.btnUnlockFingerprint.setOnClickListener { tryBiometric() }

        binding.btnUnlockPassword.setOnClickListener {
            val pwd = binding.etMasterPassword.text.toString()
            if (CryptoManager.verifyMasterPassword(this, pwd)) {
                unlockVault()
            } else {
                Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                binding.etMasterPassword.text?.clear()
            }
        }

        binding.tvShowPassword.setOnClickListener {
            binding.passwordLayout.visibility = View.VISIBLE
            binding.btnUnlockPassword.visibility = View.VISIBLE
            binding.tvShowPassword.visibility = View.GONE
        }
    }

    private fun tryBiometric() {
        if (!BiometricHelper.isBiometricAvailable(this)) return
        BiometricHelper.showBiometricPrompt(
            activity = this,
            onSuccess = { unlockVault() },
            onError = { /* show password option */ binding.passwordLayout.visibility = View.VISIBLE },
            onFailed = { Toast.makeText(this, "Fingerprint not recognised", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun unlockVault() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
