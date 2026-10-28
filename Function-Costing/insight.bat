
@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" (
    echo Usage: %~nx0 RESOURCE_GROUP_NAME
    echo Example: %~nx0 java-test-function-resource-group77701
    exit /b 1
)

set "RESOURCE_GROUP=%~1"

rem Derive Function App name from Resource Group
set "FUNCTION_APP=!RESOURCE_GROUP:-resource-group=-!"
set "APP_INSIGHTS=!FUNCTION_APP!-insights"
set "LOCATION=westeurope"

echo ========================================
echo Resource Group: !RESOURCE_GROUP!
echo Function App:   !FUNCTION_APP!
echo App Insights:   !APP_INSIGHTS!
echo ========================================

echo STEP 1: Check Application Insights
echo ========================================

call az monitor app-insights component show ^
    -g "!RESOURCE_GROUP!" ^
    --app "!APP_INSIGHTS!" >NUL 2>&1

if errorlevel 1 (
    echo Creating Application Insights...

    call az monitor app-insights component create ^
        -g "!RESOURCE_GROUP!" ^
        --app "!APP_INSIGHTS!" ^
        -l "!LOCATION!" ^
        --application-type web

    if errorlevel 1 (
        echo ERROR: Failed to create Application Insights.
        exit /b 1
    )
) else (
    echo Application Insights already exists.
)

echo ========================================
echo STEP 2: Get Connection String
echo ========================================

set "CONNECTION_STRING="

for /f "delims=" %%i in ('az monitor app-insights component show -g "!RESOURCE_GROUP!" --app "!APP_INSIGHTS!" --query connectionString -o tsv') do (
    set "CONNECTION_STRING=%%i"
)

if not defined CONNECTION_STRING (
    echo ERROR: Unable to retrieve connection string.
    exit /b 1
)

echo Connection string retrieved successfully.

echo ========================================
echo STEP 3: Configure Azure Function
echo ========================================

call az functionapp config appsettings set ^
    -g "!RESOURCE_GROUP!" ^
    -n "!FUNCTION_APP!" ^
    --settings "APPLICATIONINSIGHTS_CONNECTION_STRING=!CONNECTION_STRING!" >NUL

if errorlevel 1 (
    echo ERROR: Failed to configure Azure Function.
    exit /b 1
)

echo Application Insights configured successfully.

echo ========================================
echo STEP 4: Wait 30 Seconds
echo ========================================

timeout /t 30 /nobreak >NUL

echo ========================================
echo STEP 5: Stream Azure Function Logs
echo ========================================

call func azure functionapp logstream "!FUNCTION_APP!"

exit /b %ERRORLEVEL%
