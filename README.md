# MAX — Personal Voice Assistant & Command-to-Action Automation Engine

MAX is a fully-featured, hands-free personal voice assistant built for Android using Kotlin and Jetpack Compose. It integrates natural language intent processing, continuous background wake word listening ("MAX" / "Hey MAX"), real-time telephony control, system settings automation, and accessibility actions.

---

## 🚀 Key Features

1. **Modern Voice Hub (Jetpack Compose & Material 3)**:
   - Voice assistant status indicators (Ready, Listening, Processing, Speaking).
   - Live RMS audio visualizer waveform.
   - 3D-style glowing avatar and live transcription cards.
   - Quick command suggestion chips and system readiness summary.

2. **Wake Word & Hands-Free Operation**:
   - Configurable wake phrases: `"MAX"`, `"Hey MAX"`, `"MAX suno"`.
   - Foreground Service (`START_STICKY`) with persistent notification and mic listening.
   - Speech-to-Text (STT) via Android SpeechRecognizer and fallback triggers.

3. **Pleasant Voice Output (Text-to-Speech)**:
   - Natural female voice persona (`Locale("hi", "IN")` / `Locale.ENGLISH`).
   - Pitch & Speech rate customization (accessible in Settings).
   - Dynamic Hindi/Hinglish/English language matching.

4. **13+ Action Categories (Command-to-Action Engine)**:
   - 📞 **Phone & Incoming Calls**: Direct contact calling, Dialer launch, Incoming call HUD, Accept/Reject calls via TelecomManager & Accessibility, Speakerphone toggle.
   - 💬 **Messages & WhatsApp**: SMS composition, WhatsApp quick launch and chat opening.
   - 📱 **App Launching**: YouTube, Instagram, Camera, WhatsApp, Spotify, Settings, Chrome, Maps, Gallery, Files, Calculator.
   - 🔔 **Notifications**: Spoken notification reader via `NotificationListenerService`.
   - ⚙️ **System Controls**: Flashlight ON/OFF, Bluetooth toggles, Wi-Fi settings, Volume Up/Down/Mute, Screen Lock, Global Home/Back/Recents.
   - ⏰ **Alarms & Timers**: Set alarms and timers directly via Android system Clock intents.
   - 📅 **Calendar & Notes**: Create events and quick reminders.
   - 📍 **Navigation & Maps**: Google Maps turn-by-turn directions.
   - 🔎 **Search & Web**: Web searches and weather queries.

5. **History & Offline Persistence**:
   - Room SQLite database (`max_assistant_db`) storing command logs with timestamps, category chips, and execution status.

6. **Permissions & Privacy Transparency**:
   - 10+ granular permission status cards with 1-tap grant buttons.
   - Battery optimization guidance.
   - Privacy protection with zero hidden recording.

---

## 🛠️ Project Structure

```
├── app/
│   ├── build.gradle.kts          # Module build config (Compose, Room, KSP, Material 3)
│   └── src/main/
│       ├── AndroidManifest.xml   # Manifest with services, receivers, permissions
│       ├── java/com/example/
│       │   ├── AuraApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── data/             # Room Database, DAO, Entity
│       │   ├── engine/           # AuraVoiceEngine (STT/TTS), AuraNluParser (Intent Engine)
│       │   ├── model/            # Data Models & Action Intents
│       │   ├── services/         # ForegroundService, AccessibilityService, NotificationListener, BootReceiver
│       │   ├── telephony/        # PhoneStateReceiver, CallController
│       │   ├── ui/               # Compose Screens (VoiceHub, Commands, Automation, History, Settings)
│       │   └── viewmodel/        # AuraViewModel
│       └── res/                  # Drawables, XMLs, Strings, Themes
├── build.gradle.kts              # Root build config
└── settings.gradle.kts           # Settings & repositories
```

---

## 📦 Building the Debug APK

To build the debug APK locally or in CI/CD, run:

```bash
./gradlew assembleDebug
```

The generated APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 How to Install and Test

1. Copy `app-debug.apk` to your Android device via USB, WhatsApp, or Google Drive.
2. Tap the APK file to install (allow "Install from Unknown Sources" if prompted).
3. Open **MAX** and grant required permissions (Microphone, Phone, Accessibility).
4. Tap the glowing Mic button or speak **"MAX"** / **"Hey MAX"** to start giving voice commands!
