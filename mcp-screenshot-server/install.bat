@echo off
echo ============================================
echo VoidCodex Screenshot MCP Server Setup
echo ============================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed
    echo Please install Node.js from: https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js found:
node --version
echo.

REM Install dependencies
echo Installing dependencies...
call npm install

if errorlevel 1 (
    echo.
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

echo.
echo ============================================
echo Installation complete!
echo ============================================
echo.

REM Get current directory
set CURRENT_DIR=%cd%
set INDEX_PATH=%CURRENT_DIR%\index.js

echo Next steps:
echo.
echo 1. Add this to your Claude Code config:
echo.
echo    Windows: %%APPDATA%%\Claude\claude_desktop_config.json
echo    Location: %APPDATA%\Claude\claude_desktop_config.json
echo.
echo 2. Add this configuration:
echo.
echo {
echo   "mcpServers": {
echo     "voidcodex-screenshots": {
echo       "command": "node",
echo       "args": ["%INDEX_PATH:\=\\%"]
echo     }
echo   }
echo }
echo.
echo 3. Restart Claude Code
echo.
echo 4. Test by asking: "List my debug screenshots"
echo.
echo ============================================
echo.

REM Ask if user wants to open config file
set /p OPEN_CONFIG="Would you like to open the Claude config file now? (y/n): "
if /i "%OPEN_CONFIG%"=="y" (
    if exist "%APPDATA%\Claude\claude_desktop_config.json" (
        notepad "%APPDATA%\Claude\claude_desktop_config.json"
    ) else (
        echo.
        echo Config file doesn't exist yet. Creating it...
        mkdir "%APPDATA%\Claude" 2>nul
        echo { > "%APPDATA%\Claude\claude_desktop_config.json"
        echo   "mcpServers": { >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo     "voidcodex-screenshots": { >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo       "command": "node", >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo       "args": ["%INDEX_PATH:\=\\%"] >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo     } >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo   } >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo } >> "%APPDATA%\Claude\claude_desktop_config.json"
        echo.
        echo Config file created! Opening...
        notepad "%APPDATA%\Claude\claude_desktop_config.json"
    )
)

echo.
echo To test the server manually, run:
echo     node index.js
echo.
echo Then press Ctrl+C to stop it.
echo.
pause
