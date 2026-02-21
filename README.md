# Debukker 🐛

A powerful, plug-and-play Network Debugger and Interceptor for Kotlin Multiplatform (KMP) projects.

## Features
- **Zero Release Overhead**: Implements a Facade pattern allowing you to completely strip the library from your production builds across all KMP targets.
- **Draggable Debug Button**: A floating action button that persists its position across screens.
- **Network Logging**: View all Ktor requests and responses in real-time with detailed inspection.
- **Advanced Mocking**: Override status codes, bodies, headers, and simulate network conditions.
- **Environment Switcher**: Dynamically switch API base URLs without rebuilding.
- **Preference Manager**: View and edit on-device key-value storage (DataStore/Settings) in real-time.
- **Cross-Platform**: Android, iOS, Desktop (JVM), and Web (JS & WASM).

## Detailed Capabilities

### Network Logging
- **Real-time Monitoring**: Intercept and view every Ktor network request/response.
- **Detailed Inspection**: Dedicated screens for Headers, Query Parameters, and Request/Response bodies.
- **cURL Support**: Export any request as a standard cURL command for easy reproduction in terminals.
- **Filtering**: Search transactions by URL or method using regex or simple text.

### Network Mocking & Interception
- **Response Mocks**: Map specific URL patterns to custom status codes, bodies, and headers.
- **Request modifiers**: Inject or override headers for outgoing requests based on URL patterns.
- **Global Headers**: Add headers (like Auth tokens or App versions) globally to all outgoing requests.
- **Latency Simulation**: Artificial network throttling to test app behavior under slow connections.
- **Offline Mode**: One-tap toggle to block all network traffic.
- **Timeouts**: Override default request, connect, and socket timeouts on the fly.

### Environment Management
- **One-Tap Switching**: Toggle between Production, Pre-prod, and Local environments.
- **Custom URLs**: Enter and save a completely custom base URL for specific testing scenarios.
- **Persistence**: Your chosen environment persists across app restarts.

### Preference Management
- **Key-Value Discovery**: Automatically lists all values stored in your app's local preferences.
- **Searchable**: Quickly find specific keys in apps with large preference sets.
- **Live Editing**: Modify values on the fly to test different user states.
- **Management**: Add new keys or delete existing ones directly from the debug menu.

## Release-Safe Integration (Facade Pattern)

To ensure Debukker's UI and logic are fully removed from your release builds, we strongly recommend against adding it to `commonMain`. Instead, use the **Facade Pattern** as demonstrated in the `:sample` project.

### 1. Create a Facade in `commonMain`
Create an object in your shared module to hold references to the Debukker components. In release builds, these will be no-ops.

```kotlin
// commonMain/src/.../DebugFacade.kt
object DebugFacade {
    var DebukkerUI: @Composable () -> Unit = {}
    var httpClientFactory: () -> HttpClient = { HttpClient() }
}
```

Use this facade in your application:
```kotlin
// App.kt
@Composable
fun App() {
    val client = remember { DebugFacade.httpClientFactory() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Your App UI...
        
        // This will be a no-op in release builds, but will render a DraggableDebugButton in debug builds
        DebugFacade.DebukkerUI()
    }
}
```

### 2. Configure Dependencies (`build.gradle.kts`)
Add the library only to specific debug configurations. For Android, use `debugImplementation`. For other platforms, use a Gradle project property (e.g., `isDebug`) combined with `srcDir` re-routing.

```kotlin
val isDebug = project.hasProperty("isDebug")

kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies { if (isDebug) implementation("com.spongycode:debukker:<version>") }
            // Point to different source sets based on build type
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        val iosMain by creating {
            dependencies { if (isDebug) implementation("com.spongycode:debukker:<version>") }
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        // ... (Repeat for jsMain, wasmJsMain, etc.)
    }
}

dependencies {
    debugImplementation("com.spongycode:debukker:<version>")
}
```

### 3. Initialize on Android
Create a `DebugInitializer` object in `src/androidDebug/kotlin` and `src/androidRelease/kotlin`.

**`src/androidDebug/kotlin/.../DebugInitializer.kt`**
```kotlin
import com.spongycode.debukker.preferences.initializePreferences
import com.spongycode.debukker.ui.DraggableDebugButton
import com.spongycode.debukker.network.createDebugHttpClient

object DebugInitializer {
    fun init(context: Context) {
        initializePreferences(context)
        DebugFacade.DebukkerUI = { DraggableDebugButton() }
        DebugFacade.httpClientFactory = { createDebugHttpClient() }
    }
}
```

**`src/androidRelease/kotlin/.../DebugInitializer.kt`**
```kotlin
object DebugInitializer {
    fun init(context: Context) { /* No-op */ }
}
```

Call `DebugInitializer.init(this)` inside your `MainActivity.onCreate`.

### 4. Initialize on Non-Android Targets
Create a top-level `initDebukker()` function in `src/nonAndroidDebug/kotlin` and `src/nonAndroidRelease/kotlin`.

**`src/nonAndroidDebug/kotlin/.../DebukkerInit.kt`**
```kotlin
import com.spongycode.debukker.ui.DraggableDebugButton
import com.spongycode.debukker.network.createDebugHttpClient

fun initDebukker() {
    DebugFacade.DebukkerUI = { DraggableDebugButton() }
    DebugFacade.httpClientFactory = { createDebugHttpClient() }
}
```

**`src/nonAndroidRelease/kotlin/.../DebukkerInit.kt`**
```kotlin
fun initDebukker() { /* No-op */ }
```

Call `initDebukker()` inside your `main()` or `MainViewController()` definitions before launching your Compose app.

## Running the Sample
- **Android**: `./gradlew :sample:installDebug`
- **iOS**: Open `sample/iosApp/iosApp.xcodeproj` in Xcode (ensure you pass `-PisDebug=true` to your Gradle build phases if testing Debug mode).
- **Desktop**: `./gradlew :sample:run -PisDebug=true`
- **Web (WASM)**: `./gradlew :sample:wasmJsBrowserDevelopmentRun -PisDebug=true`
- **Web (JS)**: `./gradlew :sample:jsBrowserDevelopmentRun -PisDebug=true`