@echo off
setlocal EnableDelayedExpansion

:: === CONFIGURATION ===
set PLUGIN_YML=src\main\resources\paper-plugin.yml
set SERVER_DIR=server
set PAPER_JAR_NAME=paper.jar
set PAPER_BUILD_URL=https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/416/downloads/paper-1.20.4-416.jar

:: === STEP 0: Extract plugin name and version ===
if not exist "%PLUGIN_YML%" (
    echo ERROR: %PLUGIN_YML% not found!
    exit /b 1
)

for /f "tokens=1,* delims=:" %%A in ('findstr /b "name:" %PLUGIN_YML%') do (
    set PLUGIN_NAME=%%B
)
for /f "tokens=1,* delims=:" %%A in ('findstr /b "version:" %PLUGIN_YML%') do (
    set PLUGIN_VERSION=%%B
)

:: Trim whitespace
for /f "tokens=* delims= " %%A in ("!PLUGIN_NAME!") do set PLUGIN_NAME=%%A
for /f "tokens=* delims= " %%A in ("!PLUGIN_VERSION!") do set PLUGIN_VERSION=%%A

if "%PLUGIN_NAME%"=="" (
    echo ERROR: Could not parse plugin name
    exit /b 1
)
if "%PLUGIN_VERSION%"=="" (
    echo ERROR: Could not parse plugin version
    exit /b 1
)

set PLUGIN_JAR=%PLUGIN_NAME%-%PLUGIN_VERSION%.jar
set PLUGIN_JAR_PATH=build\libs\%PLUGIN_JAR%

:: === STEP 1: Build the Plugin ===
echo >> Building the Gradle project...
call gradlew.bat build
if errorlevel 1 (
    echo Gradle build failed!
    exit /b 1
)

:: === STEP 2: Set Up the Server If It Doesn't Exist ===
if not exist "%SERVER_DIR%\%PAPER_JAR_NAME%" (
    echo >> Setting up Paper server...
    if not exist "%SERVER_DIR%" mkdir "%SERVER_DIR%"
    powershell -Command "Invoke-WebRequest -Uri '%PAPER_BUILD_URL%' -OutFile '%SERVER_DIR%\%PAPER_JAR_NAME%'"
    if errorlevel 1 (
        echo Failed to download Paper server
        exit /b 1
    )
    echo eula=true > "%SERVER_DIR%\eula.txt"
)

:: === STEP 3: Copy Plugin to Plugins Folder ===
echo >> Copying plugin jar to plugins folder...
if not exist "%PLUGIN_JAR_PATH%" (
    echo Plugin JAR not found: %PLUGIN_JAR_PATH%
    exit /b 1
)
if not exist "%SERVER_DIR%\plugins" mkdir "%SERVER_DIR%\plugins"
copy /Y "%PLUGIN_JAR_PATH%" "%SERVER_DIR%\plugins\"

:: === STEP 4: Start the Server ===
echo >> Starting the server...
cd /d "%SERVER_DIR%"
j
