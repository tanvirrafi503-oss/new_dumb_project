# 🔐 Vault — Android Password Manager

A secure, encrypted password manager for Android with fingerprint unlock, auto-save from other apps, and manual credential management.

---

## Features

| Feature | Details |
|---|---|
| 🔐 Fingerprint unlock | Uses Android BiometricPrompt (API 26+) |
| 🔑 Master password | SHA-256 salted hash, stored in EncryptedSharedPreferences |
| 🤖 Auto-save | Accessibility Service detects login forms in other apps |
| ✍️ Manual entry | Add app name, username, password, URL, extra notes |
| 🔒 AES-256 encryption | Passwords encrypted via Android KeyStore (AES/GCM) |
| 🔍 Search | Real-time search across app names and usernames |
| 📋 Copy to clipboard | One-tap copy username or password |
| 🗑️ Delete | Long-press any credential to delete |

---

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Kotlin 1.9+
- Device running Android 8.0 (API 26) or higher

---

## How to Build

### 1. Open in Android Studio
```
File → Open → select the VaultApp folder
```

### 2. Sync Gradle
Android Studio will prompt you to sync. Click **Sync Now**.

### 3. Run on device or emulator
```
Run → Run 'app'   (Shift+F10)
```

> ⚠️ Fingerprint only works on a **real device**, not the emulator.

### 4. Build APK (for distribution)
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## First Run

1. Launch app → **Create Your Vault** screen appears
2. Set a master password (min 6 characters)
3. Grant **Accessibility Service** permission when prompted (for auto-save)
4. Done! Unlock with fingerprint or master password every time you open the app.

---

## Auto-Save: How It Works

1. Enable `Vault Auto-Save` in **Settings → Accessibility → Installed Services**
2. Open any app (e.g. Facebook, Gmail, bank app) and log in
3. Vault detects username + password fields and a submit button tap
4. A notification appears: **"Save login for [App]?"**
5. Tap the notification → review and save the credential

> **Privacy note:** All detection happens entirely on-device. Nothing leaves your phone.

---

## Architecture

```
com.vaultapp/
├── data/
│   ├── Credential.kt          ← Room entity
│   ├── CredentialDao.kt       ← DB queries
│   ├── CredentialRepository.kt ← encrypts before saving
│   └── VaultDatabase.kt       ← Room database
├── security/
│   ├── CryptoManager.kt       ← AES-256-GCM + password hashing
│   └── BiometricHelper.kt     ← Fingerprint prompt
├── service/
│   └── VaultAccessibilityService.kt ← Auto-detects login forms
└── ui/
    ├── LockActivity.kt        ← App entry point (fingerprint/password)
    ├── SetupActivity.kt       ← First-run password creation
    ├── MainActivity.kt        ← Credential list + search
    ├── EditCredentialActivity.kt ← Add / edit credential
    ├── CredentialDetailActivity.kt ← View + copy credential
    ├── SavePromptActivity.kt  ← Dialog shown after auto-detect
    ├── CredentialAdapter.kt   ← RecyclerView adapter
    └── CredentialViewModel.kt ← LiveData + coroutines
```

---

## Security Design

- **Master password** is never stored in plain text — only a salted SHA-256 hash
- **Credential passwords** are encrypted with AES-256-GCM using a key stored in the **Android KeyStore** (hardware-backed on most devices)
- **EncryptedSharedPreferences** used for the master hash store
- The database file itself is a standard Room/SQLite database; all sensitive values inside it are encrypted at the field level
- Auto-lock: the app always shows the lock screen when re-opened (no background session)

---

## Permissions Explained

| Permission | Why |
|---|---|
| `USE_BIOMETRIC` | Fingerprint unlock |
| `BIND_ACCESSIBILITY_SERVICE` | Detect login fields in other apps |
| `FOREGROUND_SERVICE` | Required for accessibility service on Android 9+ |

---

## Customisation Tips

- **Change app theme colours**: edit `res/values/themes.xml`
- **Add PIN unlock**: extend `LockActivity` with a 4-digit PIN view
- **Cloud backup**: add a Room export + Google Drive sync in `CredentialRepository`
- **Password generator**: add a "Generate" button in `EditCredentialActivity`
