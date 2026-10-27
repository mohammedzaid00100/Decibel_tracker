# Decibel Tracker

**A native Android sound meter with real-time readings, an analog gauge, locally saved measurement sessions, and PDF graph export.**

Decibel Tracker turns your Android device into a practical sound-level monitoring tool. It reads microphone input, calculates an estimated sound level, and visualizes how that level changes over time. When you start a recording session, numerical measurements are saved on-device so you can review the session later and export its graph as a PDF.

> **Measurement note:** Phone microphones and Android audio processing vary by device. Readings are estimates and are not a substitute for a calibrated, certified sound-level meter. Calibrate against a reliable reference instrument before relying on readings for technical or regulatory decisions.

## Features

- **Live sound-level measurement** — continuously updates the displayed reading using Android microphone input.
- **Analog speedometer gauge** — shows the current reading on a semicircular dial.
- **Live line graph** — visualizes recent sound-level changes in real time.
- **Record and stop** — explicitly start and finish a measurement session.
- **Local recording history** — retain multiple sessions on the device and reopen them later.
- **Session statistics** — inspect minimum, maximum, and energy-averaged level where available.
- **Reconstructed session graph** — redraw a saved graph from its stored measurement samples.
- **PDF export** — create a report with the recording details, statistics, and graph, then choose where to save it.
- **Threshold-based graph colors** — line segments change color according to their measured level:

  | Sound level | Graph color | Hex |
  |---|---|---|
  | Below 60 dB | Green | `#22C55E` |
  | 60 dB to below 85 dB | Blue | `#3B82F6` |
  | 85 dB to below 100 dB | Yellow | `#FACC15` |
  | 100 dB and above | Red | `#EF4444` |

- **Offline-first operation** — core measurement, local history, and PDF generation do not require an account or cloud service.
- **Ad-free experience** — no advertising integration is intended in the application.

## Privacy

The `RECORD_AUDIO` permission is used to access microphone input for sound-level calculation. The app processes audio samples in memory and does **not** save raw microphone audio as recordings. When the user explicitly starts a session, the app can store the derived numerical measurement samples and session details locally so the graph can be reviewed and exported. The project is designed not to transmit microphone audio or measurement sessions to external servers.

The app should request microphone access only for its measurement functionality. Review the Android manifest and dependencies when making changes to ensure the implementation continues to match these privacy statements.

## Tech stack

- **Kotlin** — application language
- **Android SDK** — native Android platform APIs
- **Jetpack Compose** and **Material 3** — UI
- **Room / SQLite** — local storage for sessions and measurement samples
- **AudioRecord** — microphone PCM input
- **Kotlin coroutines and lifecycle-aware state** — asynchronous work and UI updates
- **Android `PdfDocument`** — local PDF generation
- **Storage Access Framework** — user-selected PDF save location
- **Gradle Kotlin DSL** — build configuration

## Project structure

The project separates measurement, persistence, graph rendering, UI, and export concerns. Important components include:

```text
app/src/main/java/com/decibeltracker/app/
├── audio/
│   ├── AudioMeasurementEngine.kt
│   ├── SoundLevelCalculator.kt
│   ├── SoundLevelThresholds.kt
│   └── GraphDownsampler.kt
├── data/
│   ├── database/
│   └── repository/SoundMeterRepository.kt
├── export/PdfReportGenerator.kt
├── service/SoundMeterService.kt
└── ui/
    ├── components/
    │   ├── AnalogSpeedometerView.kt
    │   ├── LiveGraphView.kt
    │   └── SavedGraphView.kt
    └── screens/
        ├── MainScreen.kt
        ├── HistoryScreen.kt
        └── RecordingDetailsScreen.kt
```

*Note: This is an architectural overview; names or locations may differ slightly as the project evolves.*

## Getting started

### Requirements

- Android Studio with a compatible Android SDK installed
- A JDK supported by the project's Gradle version
- An Android device or emulator for running the application

### Open the project

1. Clone or download this repository.
2. Open the project root in Android Studio.
3. Allow Gradle to sync and download the dependencies.
4. Run the `app` configuration on an Android device or emulator.
5. Grant microphone permission when prompted to use live measurement.

### Build from the command line

On Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew test
./gradlew assembleDebug
```

The debug APK is normally generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

A release build intended for distribution must be signed using your own secure release key and tested before publishing.

## How recording and export work

1. Open Decibel Tracker and grant microphone permission.
2. Wait for live sound-level readings to appear.
3. Tap **Record** to create a measurement session.
4. Tap **Stop** to finish and save the session.
5. Open the recording from history to inspect its stored graph and statistics.
6. Choose **Export PDF** and select a destination in Android's save dialog.

The saved graph is reconstructed from numerical samples rather than relying on a screenshot of the live screen. Graph rendering may reduce the number of points displayed in long sessions for readability; the stored sample data should remain intact.

## Understanding the readings

The app derives a level from microphone samples using an RMS-based calculation. A digital signal level such as dBFS is not inherently the same as calibrated sound-pressure level (dB SPL). Any offset used to estimate SPL depends on the particular device and setup.

For more reliable readings, compare the app with a trusted sound-level meter and calibrate in a stable environment. A single offset may not correct frequency response, automatic gain control, or nonlinearity across every sound level. Do not use this app as a certified instrument for occupational-safety, legal, medical, or regulatory decisions.

Where the app reports an equivalent continuous level (`Leq`), it uses logarithmic energy averaging rather than treating decibel readings as ordinary linear values. The meaning and accuracy of that statistic still depend on the quality and calibration of the input measurements.

## Testing

The project includes unit tests for measurement calculations, threshold boundaries, graph downsampling, and recording-session logic. Run the tests with the commands above after making changes. Device-specific microphone behavior, background operation, and PDF output should also be checked on real Android hardware before a release.

Do not treat a successful unit-test run as proof of calibrated acoustic accuracy or as a replacement for end-to-end device testing.

## Contributing

Issues and pull requests are welcome. For useful bug reports, include:

- Android version and device model
- Steps to reproduce the issue
- Expected and observed behavior
- Relevant logs with personal or sensitive information removed
- Whether the issue affects live measurement, recording persistence, graph rendering, or PDF export

When changing recording or privacy behavior, update this README so its statements continue to reflect the implementation.

## License

No license is specified in this repository yet. Until a license is added, do not assume that the code is available for unrestricted reuse. Add a license file before explicitly granting others permission to reuse, modify, or redistribute the project.
