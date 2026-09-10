# NanAssistant - Agent Instructions

## Project Overview
Android app using Jetpack Compose (Material3), Kotlin, Gradle KTS with version catalogs. A chat assistant app that integrates with Volcano Engine (Ark) API for AI responses.

## Key Commands
```bash
# Build
./gradlew assembleDebug

# Run unit tests (host JVM)
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Configuration
- **minSdk**: 34, **targetSdk**: 37, **compileSdk**: 37
- **Kotlin**: 2.2.10, **AGP**: 9.4.0, **Compose BOM**: 2026.02.01
- Gradle configuration cache enabled (`org.gradle.configuration-cache=true`)
- Uses Tencent Maven mirror for faster downloads in China
- **Secrets**: API keys stored in `local.properties` (gitignored), loaded via `build.gradle.kts`

## Project Structure
```
app/src/main/java/li/alalin/nanassistant/
├── MainActivity.kt              # Entry point, Compose UI
├── data/
│   └── ChatPersistence.kt       # Local JSON persistence for chat history
├── network/
│   ├── ApiClient.kt             # Retrofit + OkHttp client setup
│   ├── ApiModels.kt             # Request/Response data models
│   └── VolcanoApiService.kt     # API endpoint definitions
├── ui/
│   ├── ChatViewModel.kt         # ViewModel with chat logic & persistence
│   └── theme/
│       ├── Color.kt             # Material3 color scheme
│       ├── Type.kt              # Typography
│       └── Theme.kt             # Theme composition
```

## Testing
- Unit tests: `app/src/test/` (JUnit 4)
- Instrumented tests: `app/src/androidTest/` (AndroidJUnitRunner)
- Run single test: `./gradlew test --tests "li.alalin.nanassistant.ExampleUnitTest"`

## Code Style
- Kotlin official code style (`kotlin.code.style=official` in gradle.properties)
- Java 11 source/target compatibility

## Architecture Notes
- **MVVM** with `AndroidViewModel` + Compose state (`mutableStateOf`)
- **Persistence**: JSON file in internal storage (`chat_messages.json`), loaded on ViewModel init
- **Network**: Retrofit + Moshi for Volcano Engine Ark API (DeepSeek model)
- **Threading**: ViewModelScope with `Dispatchers.Main.immediate` for UI, `Dispatchers.IO` for I/O in repository

## Function Calling
- **Tools**: `get_weather` (via wttr.in API) and `get_current_time` (local)
- **Flow**: User message → LLM with tools → Tool calls execution → Results sent back → Final response
- **Implementation**: `ChatViewModel.processChatTurn()` handles multi-turn tool calling
- **Persistence**: Tool calls and results stored in `chat_messages.json` via `PersistedMessage`/`UiMessage`
- **API Models**: `Tool`, `Function`, `ToolCall`, `FunctionCall` in `ApiModels.kt`