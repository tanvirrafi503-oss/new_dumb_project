package com.vaultapp.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vaultapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: CredentialViewModel by viewModels()
    private lateinit var adapter: CredentialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "My Vault"

        setupRecycler()
        setupSearch()
        setupFab()
        checkAccessibilityService()

        vm.allCredentials.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty())
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun setupRecycler() {
        adapter = CredentialAdapter(
            onItemClick = { cred ->
                startActivity(Intent(this, CredentialDetailActivity::class.java)
                    .putExtra("credential_id", cred.id))
            },
            onItemLongClick = { cred ->
                AlertDialog.Builder(this)
                    .setTitle("Delete \"${cred.appName}\"?")
                    .setMessage("This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> vm.delete(cred) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString()
                if (q.isBlank()) {
                    vm.allCredentials.observe(this@MainActivity) { adapter.submitList(it) }
                } else {
                    vm.search(q).observe(this@MainActivity) { adapter.submitList(it) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, EditCredentialActivity::class.java))
        }
    }

    private fun checkAccessibilityService() {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(packageName) ?: false

        if (!enabled) {
            AlertDialog.Builder(this)
                .setTitle("Enable Auto-Save")
                .setMessage("To auto-detect logins from other apps, Vault needs Accessibility permission. Enable it now?")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }
}
