@echo off
echo.
echo ========================================================
echo   VoidCodex Screenshot + Claude Code Integration
echo   Complete Setup
echo ========================================================
echo.
echo This will set up:
echo   1. Debug screenshot capture in your game (F12)
echo   2. MCP server for Claude Code integration
echo   3. Automatic screenshot review capability
echo.
pause
echo.

echo ========================================================
echo Step 1: Checking Prerequisites
echo ========================================================
echo.

REM Check Java
echo Checking Java...
javac -version >nul 2>&1
if errorlevel 1 (
    echo [!] Java not found - you'll need it to build the game
    echo     Get it from: https://adoptium.net/
) else (
    echo [✓] Java found
)

REM Check Node.js
echo Checking Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo [X] Node.js NOT found - required for MCP server
    echo.
    echo Please install Node.js from: https://nodejs.org/
    echo Then run this script again.
    pause
    exit /b 1
) else (
    echo [✓] Node.js found
)

REM Check Gradle
echo Checking Gradle...
call gradlew --version >nul 2>&1
if errorlevel 1 (
    echo [!] Gradle not responding - but that's okay
) else (
    echo [✓] Gradle found
)

echo.
echo ========================================================
echo Step 2: Installing MCP Server
echo ========================================================
echo.

cd mcp-screenshot-server

echo Installing Node.js dependencies...
call npm install

if errorlevel 1 (
    echo.
    echo [X] Failed to install dependencies
    pause
    exit /b 1
)

echo [✓] Dependencies installed
echo.

echo ========================================================
echo Step 3: Configuring Claude Code
echo ========================================================
echo.

set CURRENT_DIR=%cd%
set INDEX_PATH=%CURRENT_DIR%\index.js
set CONFIG_DIR=%APPDATA%\Claude
set CONFIG_FILE=%CONFIG_DIR%\claude_desktop_config.json

echo Configuration will be saved to:
echo %CONFIG_FILE%
echo.

REM Create config directory if it doesn't exist
if not exist "%CONFIG_DIR%" (
    echo Creating config directory...
    mkdir "%CONFIG_DIR%"
)

REM Check if config file exists
if exist "%CONFIG_FILE%" (
    echo [!] Config file already exists
    echo.
    set /p OVERWRITE="Do you want to ADD the screenshot server to existing config? (y/n): "
    if /i not "%OVERWRITE%"=="y" (
        echo.
        echo Skipping config update.
        echo You can manually add this to %CONFIG_FILE%:
        echo.
        echo   "voidcodex-screenshots": {
        echo     "command": "node",
        echo     "args": ["%INDEX_PATH:\=\\%"]
        echo   }
        echo.
        goto :test
    )
    echo.
    echo [!] Please manually add the server to your existing config
    echo     Opening the file for you...
    notepad "%CONFIG_FILE%"
    echo.
    echo Add this inside "mcpServers": { ... }
    echo.
    echo     "voidcodex-screenshots": {
    echo       "command": "node",
    echo       "args": ["%INDEX_PATH:\=\\%"]
    echo     }
    echo.
    pause
) else (
    echo Creating new config file...
    echo { > "%CONFIG_FILE%"
    echo   "mcpServers": { >> "%CONFIG_FILE%"
    echo     "voidcodex-screenshots": { >> "%CONFIG_FILE%"
    echo       "command": "node", >> "%CONFIG_FILE%"
    echo       "args": ["%INDEX_PATH:\=\\%"] >> "%CONFIG_FILE%"
    echo     } >> "%CONFIG_FILE%"
    echo   } >> "%CONFIG_FILE%"
    echo } >> "%CONFIG_FILE%"
    echo [✓] Config file created
)

:test
echo.
echo ========================================================
echo Step 4: Testing Setup
echo ========================================================
echo.

echo Running tests...
node test.js

echo.
echo ========================================================
echo Step 5: Building Game (Optional)
echo ========================================================
echo.

cd ..

set /p BUILD="Do you want to build the game now? (y/n): "
if /i "%BUILD%"=="y" (
    echo.
    echo Building game...
    call gradlew desktop:build
    echo.
    if errorlevel 1 (
        echo [!] Build had issues, but you can still run it
    ) else (
        echo [✓] Build successful
    )
)

echo.
echo ========================================================
echo   Setup Complete! 🎉
echo ========================================================
echo.
echo What you can do now:
echo.
echo 1. TEST THE INTEGRATION:
echo    Close and restart your terminal, then:
echo      claude code
echo    Then ask: "List my debug screenshots"
echo.
echo 2. RUN THE GAME:
echo      gradlew desktop:run
echo    Press F12 to capture screenshots
echo.
echo 3. AUTO-REVIEW IN CLAUDE CODE:
echo    In Claude Code, say:
echo      "Watch for new screenshots and review them"
echo.
echo ========================================================
echo Documentation:
echo   - SCREENSHOT_CLAUDE_INTEGRATION.md (How to use)
echo   - DEBUG_SCREENSHOTS.md (Screenshot system)
echo   - mcp-screenshot-server/README.md (MCP server)
echo ========================================================
echo.
echo IMPORTANT: You MUST restart Claude Code for changes to take effect!
echo.

set /p RUN="Do you want to run the game now? (y/n): "
if /i "%RUN%"=="y" (
    echo.
    echo Starting game... (Press F12 in game to capture screenshots)
    start "VoidCodex Game" gradlew desktop:run
    echo.
    echo Game starting in separate window...
    echo.
    echo Now restart Claude Code and ask me to review screenshots!
)

echo.
pause
