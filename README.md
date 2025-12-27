# WaqiKids Launcher

Kid-friendly Android launcher with parental controls.

## Features

- 🏠 **Custom Launcher** - Replaces default home screen, shows only approved apps
- 🛡️ **Accessibility Service** - Blocks access to Settings and Play Store
- 🌐 **DNS Integration** - Works with WaqiKids DNS for web filtering
- 📱 **Parent Control** - Remote app management via WaqiKids parent app
- ⏰ **Time Limits** - Screen time management (coming soon)

## Architecture

- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt
- **Navigation**: Compose Navigation
- **Networking**: Retrofit + OkHttp
- **Storage**: DataStore Preferences

## Project Structure

```
app/src/main/java/com/waqikids/launcher/
├── WaqiKidsApp.kt              # Application class
├── ui/                         # Compose UI screens
│   ├── MainActivity.kt
│   ├── theme/                  # Theme, colors, typography
│   ├── navigation/             # Nav routes and host
│   ├── splash/
│   ├── onboarding/
│   ├── pairing/
│   ├── setup/
│   └── launcher/               # Main home screen
├── domain/model/               # Data models
├── data/
│   ├── api/                    # Retrofit API
│   ├── local/                  # DataStore preferences
│   └── repository/             # Data repositories
├── service/                    # Android services
│   ├── WaqiAccessibilityService.kt
│   ├── WaqiDeviceAdminReceiver.kt
│   ├── BootReceiver.kt
│   ├── PackageChangeReceiver.kt
│   └── SyncService.kt
├── di/                         # Hilt modules
└── util/                       # Utilities and constants
```

## Building

1. Open in Android Studio
2. Sync Gradle
3. Build > Make Project
4. Run on device/emulator

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 35
- Kotlin 2.0+
- JDK 17

## Protection Modes

### Easy Mode
- Launcher hides unapproved apps
- Accessibility blocks Settings & Play Store
- Private DNS filters websites
- Can be bypassed with factory reset

### Fort Knox Mode
- Everything in Easy Mode
- Device Owner enabled
- Cannot uninstall launcher
- Cannot disable protections
- Unbreakable - requires factory reset

## Testing

To test as a launcher:
1. Install on device
2. Press home button
3. Select "WaqiKids" as launcher
4. Choose "Always" or "Just Once"

## License

Proprietary - WaqiKids
