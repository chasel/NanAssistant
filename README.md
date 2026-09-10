# NanAssistant

AI-powered chat assistant Android app built with Jetpack Compose and Material3, integrating with Volcano Engine (Ark) API.

## Features
- 💬 Real-time chat with AI assistant (DeepSeek model via Volcano Engine)
- 🛠️ **Function Calling**: Weather queries & current time via tool calls
- 💾 Local chat history persistence (JSON)
- 🎨 Material3 theming with dynamic colors
- ⚡ Modern Android stack: Compose, ViewModel, Coroutines, Retrofit, Moshi

## Screenshots
<!-- Add screenshots here -->

## Tech Stack
| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material3 |
| Architecture | MVVM, AndroidViewModel |
| Network | Retrofit, OkHttp, Moshi |
| Persistence | JSON file (internal storage) |
| Async | Kotlin Coroutines, Flow |
| Build | Gradle KTS, Version Catalogs |

## Requirements
- Android Studio Ladybug+ (2024.2+)
- JDK 11+
- Android SDK 34+

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd NanAssistant
   ```

2. **Configure API keys**
   Create/edit `local.properties` in the project root:
   ```properties
   sdk.dir=/path/to/android/sdk
   VOLCANO_API_KEY=your_api_key_here
   VOLCANO_MODEL_ID=deepseek-v4-flash-ga-260731
   ```
   > ⚠️ `local.properties` is gitignored. Never commit API keys.

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Install the generated APK on your device/emulator.

## Project Structure
```
app/src/main/java/li/alalin/nanassistant/
├── MainActivity.kt              # Compose UI entry point
├── data/
│   └── ChatPersistence.kt       # Local JSON chat storage
├── network/
│   ├── ApiClient.kt             # Retrofit/OkHttp setup
│   ├── ApiModels.kt             # Request/Response models (incl. Function Calling)
│   ├── VolcanoApiService.kt     # API endpoints
│   ├── WeatherApiService.kt     # Weather API (wttr.in)
│   └── WeatherApiModels.kt      # Weather response models
├── ui/
│   ├── ChatViewModel.kt         # ViewModel + chat logic & Function Calling
│   └── theme/                   # Material3 theming
```

## Configuration
- **minSdk**: 34
- **targetSdk**: 37
- **compileSdk**: 37
- **Kotlin**: 2.2.10
- **AGP**: 9.4.0
- **Compose BOM**: 2026.02.01

## Commands
```bash
./gradlew assembleDebug     # Build debug APK
./gradlew test              # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
./gradlew lint              # Run lint checks
./gradlew clean             # Clean build
```

## Architecture
- **MVVM** with `AndroidViewModel` + Compose `mutableStateOf`
- Chat history persisted to `chat_messages.json` in internal storage
- Messages loaded on ViewModel initialization
- Network calls on `Dispatchers.IO`, UI updates on `Dispatchers.Main.immediate`

### Function Calling
- **Tools**: `get_weather` (wttr.in API) and `get_current_time` (local)
- **Flow**: User message → LLM with tools → Tool execution → Results returned → Final response
- **Implementation**: `ChatViewModel.processChatTurn()` handles multi-turn tool calling loop
- **Models**: `Tool`, `Function`, `ToolCall`, `FunctionCall` in `ApiModels.kt`
- **Persistence**: Tool calls/results stored via `PersistedMessage`/`UiMessage`

## Security
- API keys stored in `local.properties` (excluded from git)
- Keys injected at build time via `BuildConfig`
- No secrets in source code or version control

## License
MIT License - feel free to use and modify.