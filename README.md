# Nova Assistant 🤖🎙️

> **Gemini-Powered Native Android Voice Assistant** with conversational AI persona, hardware device controls, Google Gemini Settings UI, and trained natural human voices.

---

## 🚀 Automatic APK Building & Direct Download

This repository includes an automated **GitHub Actions CI/CD workflow** (`.github/workflows/build-apk.yml`) that automatically compiles the Android Debug APK and publishes it for direct downloading.

### 📥 How to Download the APK:

#### Option 1: Direct from GitHub Releases (Recommended)
1. Go to the **[Releases](https://github.com/${{ github.repository }}/releases)** page of this GitHub repository.
2. Under the latest release, click on **`NovaAssistant-v1.0-debug-b*.apk`** (or `app-debug.apk`) under **Assets**.
3. Transfer or download the APK directly onto your Android phone and install it.

#### Option 2: From GitHub Actions Artifacts
1. Go to the **Actions** tab on GitHub.
2. Click on the latest run under **"Build & Release Android Debug APK"**.
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click **`NovaAssistant-Debug-APK-b*`** to download the ZIP file containing the APK.

#### Option 3: Manual Trigger ("Run workflow")
1. Open the **Actions** tab in your repository.
2. Select **"Build & Release Android Debug APK"** from the left sidebar.
3. Click the **"Run workflow"** button on the right.
4. (Optional) Toggle release creation or customize the version tag.
5. Click **"Run workflow"** — in ~2 minutes your fresh APK will be ready and published!

---

## 🔑 (Optional) Setting up Gemini API Key in GitHub Secrets

To have the APK pre-configured with your Gemini API Key during the build:
1. In your GitHub repository, go to **Settings** > **Secrets and variables** > **Actions**.
2. Click **New repository secret**.
3. Name: `GEMINI_API_KEY`
4. Value: Paste your Google Gemini API Key.
5. Click **Add secret**.

*(Note: Even if no secret is set, you can easily enter or test your Gemini API Key directly inside the app under **Settings > Google Gemini Settings**).*

---

## 🛠️ Local Build Instructions

If building locally using Android Studio or command-line Gradle:

```bash
# Clone the repository
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>

# Decode debug keystore (if not already present)
base64 -d debug.keystore.base64 > debug.keystore

# Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## ✨ Features
- **Native Trained Human Voices**: Gemini neural voices (Orbit, Flare, Glow, Capella, Orion, Aoede, Puck, etc.) with 24 kHz high-fidelity streaming.
- **Hardware Integration**: Voice control for Flashlight, Camera, Phone calls, Timers, YouTube, and WhatsApp.
- **Google Gemini Settings UI**: Complete voice selection dialog, voice previews, barge-in / interruption settings, and live API key validation.
- **Material 3 Design & Waveform Aura**: Edge-to-edge design with dynamic sound wave visualizer.
