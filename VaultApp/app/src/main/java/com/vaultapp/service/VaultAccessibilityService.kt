package com.vaultapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.vaultapp.R
import com.vaultapp.ui.SavePromptActivity

/**
 * Monitors foreground apps for login forms and triggers a save-prompt notification
 * when username + password fields are detected and submitted.
 */
class VaultAccessibilityService : AccessibilityService() {

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_PASSWORD = "extra_password"
        private const val CHANNEL_ID = "vault_autosave"
        private const val NOTIF_ID = 1001
    }

    private var lastPackage = ""
    private var capturedUsername = ""
    private var capturedPassword = ""
    private var capturedAppName = ""

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        createNotificationChannel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return  // ignore our own app

        // Track app change → reset captures
        if (pkg != lastPackage) {
            lastPackage = pkg
            capturedUsername = ""
            capturedPassword = ""
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                scanWindowForCredentials(event.source, pkg)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // Check if a submit/login button was clicked after credentials captured
                val nodeText = event.source?.text?.toString()?.lowercase() ?: ""
                if (capturedUsername.isNotBlank() && capturedPassword.isNotBlank() &&
                    (nodeText.contains("login") || nodeText.contains("sign in") ||
                     nodeText.contains("log in") || nodeText.contains("submit") ||
                     nodeText.contains("continue"))
                ) {
                    triggerSavePrompt(pkg, capturedUsername, capturedPassword)
                }
            }
        }
    }

    private fun scanWindowForCredentials(node: AccessibilityNodeInfo?, pkg: String) {
        node ?: return
        traverseNode(node, pkg)
    }

    private fun traverseNode(node: AccessibilityNodeInfo, pkg: String) {
        if (node.className?.contains("EditText") == true) {
            val text = node.text?.toString() ?: ""
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

            val isPassword = node.isPassword
            val isUsername = !isPassword && (
                hint.contains("email") || hint.contains("username") ||
                hint.contains("phone") || hint.contains("user") ||
                viewId.contains("email") || viewId.contains("user") ||
                viewId.contains("login") || viewId.contains("phone") ||
                contentDesc.contains("email") || contentDesc.contains("username")
            )

            if (isPassword && text.isNotBlank()) {
                capturedPassword = text
            } else if (isUsername && text.isNotBlank()) {
                capturedUsername = text
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, pkg)
            child.recycle()
        }
    }

    private fun triggerSavePrompt(pkg: String, username: String, password: String) {
        if (username.isBlank() || password.isBlank()) return

        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(pkg, 0)
            ).toString()
        } catch (e: Exception) { pkg }

        // Show a notification to prompt saving
        val intent = Intent(this, SavePromptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PACKAGE, pkg)
            putExtra(EXTRA_APP_NAME, appName)
            putExtra(EXTRA_USERNAME, username)
            putExtra(EXTRA_PASSWORD, password)
        }

        val pi = PendingIntent.getActivity(
            this, NOTIF_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vault)
            .setContentTitle("Save login for $appName?")
            .setContentText("Tap to save credentials to Vault")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, notif)

        // Reset so we don't spam
        capturedUsername = ""
        capturedPassword = ""
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Auto-Save Prompts",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Prompts to save credentials detected in other apps"
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onInterrupt() {}
}
