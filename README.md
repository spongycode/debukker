# Debukker 🐛

A powerful, plug-and-play Network Debugger and Interceptor for Kotlin Multiplatform (KMP) projects.

## Features
- **Zero Release Overhead**: Implements a Facade pattern allowing you to completely strip the library from your production builds across all KMP targets.
- **Draggable Debug Button**: A floating action button that persists its position across screens.
- **Network Logging**: View all Ktor requests and responses in real-time.
- **Request/Response Mocking**: Override status codes, bodies, headers, and simulate delays.
- **Environment Switcher**: Dynamically switch your API base URLs (Production, Staging, Local, Custom) without rebuilding.
- **Cross-Platform**: Android, iOS, Desktop (JVM), and Web (JS & WASM).

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