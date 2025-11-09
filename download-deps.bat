@echo off
REM Simple batch script to download Maven dependencies

cd /d c:\coding\javaProjects\InterviewAI

echo.
echo Downloading Maven dependencies...
echo This may take a few minutes on first run...
echo.

REM Try to find and run Maven
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Found Maven in PATH
    call mvn dependency:resolve -q
    call mvn clean compile -q
    echo.
    echo Dependencies downloaded successfully!
) else (
    echo Maven not found in PATH
    echo Please install Maven or add it to your PATH
    echo Visit: https://maven.apache.org/download.cgi
    pause
)
