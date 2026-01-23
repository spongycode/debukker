# Debukker 🐛

A powerful, plug-and-play Network Debugger and Interceptor for Kotlin Multiplatform (KMP) projects.

## Features
- **Network Logging**: View all Ktor requests and responses in real-time.
- **Request/Response Mocking**: Override status codes, bodies, headers, and simulate delays.
- **Network Controls**: Toggle Offline Mode or simulate slow networks with Throttling.
- **Cross-Platform Persistence**: Settings and mocks persist across app restarts using platform-specific storage.
- **Multi-platform Support**: Android, iOS, Desktop (JVM), and Web (JS & WASM).

## Project Structure
- `:debukker`: The core library module.
- `:sample`: A multi-platform sample app demonstrating integration.

## Integration

### 1. Add Dependency
Add the Debukker library to your shared module's `commonMain` dependencies:

```kotlin
commonMain.dependencies {
    implementation(project(":debukker"))
}
```

### 2. Configure Ktor Client
Install the `DebugNetworkPlugin` into your Ktor `HttpClient`:

```kotlin
import com.spongycode.debukker.network.DebugNetworkPlugin

val client = HttpClient {
    install(DebugNetworkPlugin)
}

// Or use the helper factory
val client = createDebugHttpClient()
```

### 3. Initialize Storage (Android only)
In your Android `MainActivity`, initialize the debug preferences:

```kotlin
import com.spongycode.debukker.preferences.initializePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePreferences(this)
        setContent { App() }
    }
}
```

### 4. Show Debug Menu
Drop the `DebugMenu` composable into your UI:

```kotlin
import com.spongycode.debukker.ui.DebugMenu

@Composable
fun App() {
    var showDebugMenu by remember { mutableStateOf(false) }

    Box {
        // Your App UI
        
        DebugMenu(
            isVisible = showDebugMenu,
            onDismiss = { showDebugMenu = false }
        )
    }
}
```

## Running the Sample
- **Android**: `./gradlew :sample:installDebug`
- **iOS**: Open `sample/iosApp/iosApp.xcodeproj` in Xcode.
- **Desktop**: `./gradlew :sample:run`
- **Web (WASM)**: `./gradlew :sample:wasmJsBrowserDevelopmentRun`
- **Web (JS)**: `./gradlew :sample:jsBrowserDevelopmentRun`