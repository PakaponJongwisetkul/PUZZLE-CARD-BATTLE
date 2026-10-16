@echo off
cd /d "%~dp0"

if exist "%~dp0\mvnw.cmd" (
    call "%~dp0\mvnw.cmd" -q exec:java
    if errorlevel 1 (
        echo.
        echo JavaFX app failed to start.
        pause
    )
    exit /b %errorlevel%
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is missing. This project needs Maven 3.9+ to download JavaFX dependencies.
    echo Please install Maven or use the bundled wrapper.
    echo.
    pause
    exit /b 1
)

call mvn -q exec:java
if errorlevel 1 (
    echo.
    echo Failed to launch the game.
    pause
)
