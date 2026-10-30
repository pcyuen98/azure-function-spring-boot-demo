
@echo off
setlocal EnableExtensions

if "%~1"=="" (
    echo Usage: %~nx0 RESOURCE_GROUP_NAME
    echo Example: %~nx0 java-test-function-resource-group77705
    exit /b 1
)

set "RESOURCE_GROUP=%~1"
set "FUNCTION_APP="

echo Finding Function App in resource group %RESOURCE_GROUP%...

REM Automatically find the Function App
for /f "delims=" %%A in ('az functionapp list --resource-group "%RESOURCE_GROUP%" --query "[0].name" --output tsv') do (
    set "FUNCTION_APP=%%A"
)

if not defined FUNCTION_APP (
    echo ERROR: No Function App found in resource group %RESOURCE_GROUP%.
    exit /b 1
)

echo Found Function App: %FUNCTION_APP%
echo Enabling TRACE logging...

az functionapp config appsettings set ^
 --resource-group "%RESOURCE_GROUP%" ^
 --name "%FUNCTION_APP%" ^
 --settings ^
 "AzureFunctionsJobHost__logging__logLevel__default=Trace" ^
 "AzureFunctionsJobHost__logging__logLevel__Host=Trace" ^
 "AzureFunctionsJobHost__logging__logLevel__Function=Trace" ^
 "AzureFunctionsJobHost__logging__logLevel__Worker=Trace" ^
 "AzureFunctionsJobHost__logging__logLevel__Host.Results=Trace" ^
 "AzureFunctionsJobHost__logging__applicationInsights__samplingSettings__isEnabled=false" ^
 "LOGGING_LEVEL_ROOT=TRACE" ^
 "LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=TRACE" ^
 "LOGGING_LEVEL_COM_BELISTY=TRACE" ^
 "LOGGING_LEVEL_COM_UTIL_FUNCTION=TRACE" ^
 "LOGGING_LEVEL_COM_AZURE=DEBUG" ^
 --output none

if errorlevel 1 (
    echo ERROR: Failed to configure logging.
    exit /b 1
)

echo SUCCESS: Logging settings applied.
echo Restarting Function App...

az functionapp restart ^
 --resource-group "%RESOURCE_GROUP%" ^
 --name "%FUNCTION_APP%"

if errorlevel 1 (
    echo ERROR: Restart failed.
    exit /b 1
)

echo SUCCESS: Function App restarted.
exit /b 0
