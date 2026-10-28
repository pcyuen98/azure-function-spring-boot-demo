
@echo off
setlocal

echo ==========================================
echo AZURE ACCOUNT INFORMATION
echo ==========================================

REM Check Azure login
call az account show >nul 2>&1
if errorlevel 1 (
    call az login
    if errorlevel 1 exit /b 1
)

REM Retrieve Azure account details
for /f "delims=" %%A in ('az account show --query id -o tsv') do set "SUBSCRIPTION_ID=%%A"
for /f "delims=" %%A in ('az account show --query tenantId -o tsv') do set "TENANT_ID=%%A"
for /f "delims=" %%A in ('az account show --query user.name -o tsv') do set "USERNAME=%%A"

echo.
echo Subscription ID : %SUBSCRIPTION_ID%
echo Tenant ID       : %TENANT_ID%
echo Username        : %USERNAME%
echo ==========================================

endlocal
