package com.vaultapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vaultapp.data.Credential
import com.vaultapp.databinding.ItemCredentialBinding

class CredentialAdapter(
    private val onItemClick: (Credential) -> Unit,
    private val onItemLongClick: (Credential) -> Unit
) : ListAdapter<Credential, CredentialAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemCredentialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCredentialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cred = getItem(position)
        holder.binding.apply {
            tvAppName.text = cred.appName.ifBlank { cred.packageName }
            tvUsername.text = cred.username
            tvAutoSaved.visibility = if (cred.isAutoSaved)
                android.view.View.VISIBLE else android.view.View.GONE
            root.setOnClickListener { onItemClick(cred) }
            root.setOnLongClickListener { onItemLongClick(cred); true }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Credential>() {
            override fun areItemsTheSame(a: Credential, b: Credential) = a.id == b.id
            override fun areContentsTheSame(a: Credential, b: Credential) = a == b
        }
    }
}
