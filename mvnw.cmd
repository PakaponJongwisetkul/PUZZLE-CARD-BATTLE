@echo off
setlocal
cd /d "%~dp0"

set "MAVEN_HOME=%~dp0\.mvn\apache-maven-3.9.9"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MAVEN_BIN%" (
    if not exist "%~dp0\.mvn" mkdir "%~dp0\.mvn"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $url='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip'; $zip='%~dp0\.mvn\apache-maven-3.9.9-bin.zip'; Invoke-WebRequest -Uri $url -OutFile $zip; Expand-Archive -Path $zip -DestinationPath '%~dp0\.mvn' -Force"
)

if not exist "%MAVEN_BIN%" (
    echo Failed to download Maven. Please check your internet connection and try again.
    exit /b 1
)

call "%MAVEN_BIN%" %*
