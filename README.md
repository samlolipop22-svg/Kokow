# Glass Launcher (Android TV)
Open this folder in Android Studio (Koala+), let Gradle sync, and run on an Android TV device or emulator.

Make it the home screen:
- Press Home on the TV and choose Glass Launcher, or
- `adb shell cmd package set-home-activity com.glass.launcher/.MainActivity`

Controls: D-pad to browse, OK to open, hold OK for App info. Back is ignored.
