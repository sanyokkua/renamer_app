@echo off
setlocal enabledelayedexpansion

REM Build Windows executable and .msi installer
REM Requires: JDK 25 with jpackage, WiX Toolset 3.x for MSI

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "UI_TARGET=%PROJECT_ROOT%\app\ui\target"
set "INPUT_DIR=%UI_TARGET%\libs"
set "OUTPUT_DIR=%PROJECT_ROOT%\dist"
set "ICON=%PROJECT_ROOT%\icon.ico"

REM Find the main JAR
for %%F in ("%INPUT_DIR%\ua.renamer.app.ui-*.jar") do set "MAIN_JAR=%%~nxF"

if "%MAIN_JAR%"=="" (
    echo ERROR: Could not find ua.renamer.app.ui-*.jar in %INPUT_DIR%
    exit /b 1
)

if not exist "%INPUT_DIR%" (
    echo ERROR: %INPUT_DIR% not found. Run 'cd app ^&^& mvn clean package -DskipTests' first.
    exit /b 1
)

if not defined APP_VERSION set "APP_VERSION=2.0.0"

set "APP_NAME=Renamer"
set "JLINK_OPTIONS=--strip-debug --no-header-files --no-man-pages --compress zip-6"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo App: %APP_NAME% v%APP_VERSION%
echo Main JAR: %MAIN_JAR%

REM Build app-image (raw executable)
echo === Building Windows app-image ===
if exist "%OUTPUT_DIR%\%APP_NAME%" rmdir /s /q "%OUTPUT_DIR%\%APP_NAME%"

jpackage ^
    --input "%INPUT_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --name "%APP_NAME%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class ua.renamer.app.Launcher ^
    --app-version "%APP_VERSION%" ^
    --vendor "Renamer App" ^
    --description "Batch file renaming application" ^
    --icon "%ICON%" ^
    --java-options "--enable-preview" ^
    --java-options "-Xmx512m" ^
    --jlink-options "%JLINK_OPTIONS%" ^
    --type app-image

echo App image created: %OUTPUT_DIR%\%APP_NAME%\

REM Build .msi installer (requires WiX Toolset)
echo === Building Windows .msi ===
jpackage ^
    --input "%INPUT_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --name "%APP_NAME%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class ua.renamer.app.Launcher ^
    --app-version "%APP_VERSION%" ^
    --vendor "Renamer App" ^
    --description "Batch file renaming application" ^
    --icon "%ICON%" ^
    --java-options "--enable-preview" ^
    --java-options "-Xmx512m" ^
    --jlink-options "%JLINK_OPTIONS%" ^
    --type msi ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "Renamer" ^
    --win-shortcut

echo === Windows packaging complete ===
dir "%OUTPUT_DIR%\*.msi" 2>nul
