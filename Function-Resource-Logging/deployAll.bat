
@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" (
    echo Usage: %~nx0 RESOURCE_GROUP_NAME
    exit /b 1
)

set "RESOURCE_GROUP=%~1"

echo Checking resource group: %RESOURCE_GROUP%

for /f %%A in ('az group exists --name "%RESOURCE_GROUP%"') do set "EXISTS=%%A"

if /I "!EXISTS!"=="true" (
    echo Deleting resource group: %RESOURCE_GROUP%
    call az group delete --name "%RESOURCE_GROUP%" --yes --no-wait

    if errorlevel 1 (
        echo ERROR: Resource group deletion failed.
        exit /b 1
    )
) else (
    echo Resource group does not exist. Skipping deletion.
)

echo ========================================
echo STEP 1: Maven Build
echo ========================================

call mvn clean install -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed. Stopping.
    exit /b 1
)

echo ========================================
echo STEP 2: Terraform Init
echo ========================================

cd terraform || exit /b 1

call terraform init
if errorlevel 1 (
    echo ERROR: Terraform init failed. Stopping.
    exit /b 1
)

echo ========================================
echo STEP 3: Terraform Plan
echo ========================================

call terraform plan -out=plan.tfplan
if errorlevel 1 (
    echo ERROR: Terraform plan failed. Stopping.
    exit /b 1
)

echo ========================================
echo STEP 4: Terraform Apply
echo ========================================

call terraform apply plan.tfplan
if errorlevel 1 (
    echo ERROR: Terraform apply failed. Stopping.
    exit /b 1
)

cd ..

echo ========================================
echo STEP 5: Maven Azure Deployment
echo ========================================

call mvn clean package azure-functions:deploy -DskipTests
if errorlevel 1 (
    echo ERROR: Azure Function deployment failed.
    exit /b 1
)

echo ========================================
echo STEP 6: Azure Function Health Check
echo ========================================

set "URL=https://%RESOURCE_GROUP%.azurewebsites.net/api/gethistory"
set "MAX_RETRIES=12"
set "WAIT_SECONDS=5"

echo Checking Azure Function...
echo URL: !URL!

for /L %%I in (1,1,!MAX_RETRIES!) do (
    set "HTTP_CODE="

    for /f %%H in ('curl.exe -L -s -o NUL -w "%%{http_code}" --connect-timeout 3 --max-time 5 "!URL!"') do (
        set "HTTP_CODE=%%H"
    )

    echo Attempt %%I/!MAX_RETRIES! - HTTP !HTTP_CODE!

    if "!HTTP_CODE!"=="200" (
        echo SUCCESS: Azure Function is responding.
        exit /b 0
    )

    if %%I LSS !MAX_RETRIES! (
        timeout /t !WAIT_SECONDS! /nobreak >nul
    )
)

echo ERROR: Azure Function did not return HTTP 200.
exit /b 1
