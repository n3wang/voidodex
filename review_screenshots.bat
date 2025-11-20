@echo off
echo ============================================
echo Debug Screenshot Reviewer for VoidCodex
echo ============================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3 from python.org
    pause
    exit /b 1
)

REM Check if anthropic package is installed
python -c "import anthropic" >nul 2>&1
if errorlevel 1 (
    echo Installing required package: anthropic
    pip install anthropic
    echo.
)

REM Check for API key
if "%ANTHROPIC_API_KEY%"=="" (
    echo.
    echo WARNING: ANTHROPIC_API_KEY environment variable not set
    echo.
    echo To set it temporarily for this session:
    echo   set ANTHROPIC_API_KEY=your_key_here
    echo.
    echo To set it permanently:
    echo   setx ANTHROPIC_API_KEY "your_key_here"
    echo.
    echo Get your API key from: https://console.anthropic.com/
    echo.
    set /p TEMP_API_KEY="Enter your API key now (or press Enter to exit): "
    if "%TEMP_API_KEY%"=="" (
        echo Exiting...
        pause
        exit /b 1
    )
    set ANTHROPIC_API_KEY=%TEMP_API_KEY%
)

echo.
echo Choose an option:
echo   1. Review all screenshots (batch mode)
echo   2. Watch for new screenshots (real-time mode)
echo   3. Exit
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Running batch review...
    python debug_screenshot_reviewer.py
) else if "%choice%"=="2" (
    echo.
    echo Starting watch mode... (Press Ctrl+C to stop)
    python debug_screenshot_reviewer.py --watch
) else (
    echo Exiting...
    exit /b 0
)

echo.
echo.
echo ============================================
echo Review complete!
echo ============================================
pause
