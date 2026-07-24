@echo off
set "JAVA_HOME=D:\Program Files\Eclipse Adoptium\jre-21.0.11.10-hotspot"
set "ANDROID_HOME=D:\Tools\android-sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\emulator;%ANDROID_HOME%\platform-tools;%PATH%"

echo Starting Redmi Pad 2 Android Emulator...
start "" "%ANDROID_HOME%\emulator\emulator.exe" -avd Redmi_Pad_2 -gpu auto
