
@echo off
setlocal EnableExtensions

REM ==========================================
REM Azure Function Build, Deploy and Test
REM Usage: deploy.bat APP_NAME RESOURCE_GROUP
REM ==========================================

if "%~1"=="" goto usage
if "%~2"=="" goto usage

set "APP=%~1"
set "RG=%~2"
set "URL=https://%APP%.azurewebsites.net/api/gethistory"
set "STAGING=target\azure-functions\%APP%"

cd /d "%~dp0"

echo ==========================================
echo Function App   : %APP%
echo Resource Group : %RG%
echo Test URL       : %URL%
echo ==========================================

REM Step 1: Maven build
echo.
echo [1/4] Building Maven project...

call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed.
    exit /b 1
)

REM Step 2: Deploy
echo.
echo [2/4] Publishing Azure Function...

if not exist "%STAGING%\host.json" (
    echo ERROR: Function package not found.
    exit /b 1
)

pushd "%STAGING%"
call func azure functionapp publish "%APP%" --java
set "PUBLISH_RC=%ERRORLEVEL%"
popd

if not "%PUBLISH_RC%"=="0" (
    echo WARNING: Publish reported an error.
    echo Continuing to start and test the app...
)

REM Step 3: Start Function App
echo.
echo [3/4] Starting Azure Function App...

call az functionapp start -g "%RG%" -n "%APP%"

if errorlevel 1 (
    echo ERROR: Failed to start Function App.
    exit /b 1
)

REM Step 4: Retry until HTTP 200
echo.
echo [4/4] Waiting for Function App readiness...
echo URL: %URL%
echo Press Ctrl+C to stop.
echo.

set /a ATTEMPT=0

:retry
set /a ATTEMPT+=1
set "HTTP_CODE="

for /f "delims=" %%H in ('curl.exe -sS -L -o NUL -w "%%{http_code}" --connect-timeout 10 --max-time 30 "%URL%" 2^>NUL') do set "HTTP_CODE=%%H"

if "%HTTP_CODE%"=="200" goto success

if not defined HTTP_CODE set "HTTP_CODE=000"

echo Attempt %ATTEMPT%: HTTP %HTTP_CODE%
echo Retrying in 10 seconds...

timeout /t 10 /nobreak >NUL
goto retry

:success
echo.
echo ==========================================
echo SUCCESS: Azure Function is ready!
echo Function App : %APP%
echo HTTP Status  : 200
echo URL          : %URL%
echo Attempts     : %ATTEMPT%
echo ==========================================

exit /b 0

:usage
echo Usage: %~nx0 APP_NAME RESOURCE_GROUP
echo Example:
echo %~nx0 java-test-function-77705 java-test-function-resource-group77705
exit /b 1
