
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "OLD_SUB=5c3579a2-4d66-4bee-a612-e66b89fbf8e9"
set "ROLE=azurerm_role_assignment.function_owner"

echo ==========================================
echo AZURE TERRAFORM TENANT AND STATE FIX
echo ==========================================

REM 1. Check Azure login
echo [1/8] Checking Azure login...
call az account show >nul 2>&1
if errorlevel 1 (
    call az login
    if errorlevel 1 goto :error
)

REM 2. Get current tenant and subscription
echo [2/8] Reading Azure account...

set "TENANT_ID="
set "SUBSCRIPTION_ID="

for /f "delims=" %%A in ('az account show --query tenantId -o tsv') do set "TENANT_ID=%%A"
for /f "delims=" %%A in ('az account show --query id -o tsv') do set "SUBSCRIPTION_ID=%%A"

if not defined TENANT_ID goto :error
if not defined SUBSCRIPTION_ID goto :error

echo Tenant       : !TENANT_ID!
echo Subscription : !SUBSCRIPTION_ID!

REM 3. Configure Terraform authentication
echo [3/8] Configuring Terraform authentication...

set "ARM_CLIENT_ID="
set "ARM_CLIENT_SECRET="
set "ARM_USE_OIDC="
set "ARM_USE_MSI="
set "ARM_USE_CLI=true"
set "ARM_TENANT_ID=!TENANT_ID!"
set "ARM_SUBSCRIPTION_ID=!SUBSCRIPTION_ID!"

REM 4. Find main.tf
echo [4/8] Finding Terraform project...

set "TF_DIR="

if exist "%~dp0terraform\main.tf" (
    set "TF_DIR=%~dp0terraform"
)

if not defined TF_DIR (
    for /r "%~dp0" %%F in (main.tf) do (
        if not defined TF_DIR set "TF_DIR=%%~dpF"
    )
)

if not defined TF_DIR (
    echo ERROR: main.tf not found.
    goto :error
)

pushd "!TF_DIR!"
if errorlevel 1 goto :error
set "PUSHED=1"

echo Directory: !TF_DIR!

REM 5. Initialize Terraform
echo [5/8] Initializing Terraform...

call terraform init -reconfigure
if errorlevel 1 goto :error

REM 6. Back up Terraform state
echo [6/8] Backing up Terraform state...

set "BACKUP=terraform-state-backup-%RANDOM%-%RANDOM%.json"

call terraform state pull > "!BACKUP!"
if errorlevel 1 goto :error

for %%F in ("!BACKUP!") do (
    if %%~zF EQU 0 (
        echo ERROR: State backup is empty.
        goto :error
    )
)

echo Backup: !BACKUP!

REM 7. Inspect and remove old role assignment
echo [7/8] Checking old role assignment...

set "ROLE_ID="

for /f "tokens=1,* delims==" %%A in ('terraform state show -no-color "!ROLE!" 2^>nul ^| findstr /R /C:"^[ ]*id[ ]*="') do (
    set "ROLE_ID=%%B"
)

if defined ROLE_ID (
    echo Existing role assignment: !ROLE_ID!

    echo !ROLE_ID! | findstr /I /C:"!OLD_SUB!" >nul
    if not errorlevel 1 (
        echo Old subscription detected.
        echo Removing role assignment from Terraform state...

        call terraform state rm "!ROLE!"
        if errorlevel 1 goto :error
    ) else (
        echo Role assignment does not match old subscription.
        echo Skipping state removal.
    )
) else (
    echo Role assignment not found in state.
)

REM 8. Validate and plan
echo [8/8] Running Terraform validation and plan...

call terraform validate
if errorlevel 1 goto :error

call terraform plan
if errorlevel 1 goto :error

echo.
echo ==========================================
echo SUCCESS - TERRAFORM PLAN COMPLETED
echo ==========================================
echo Tenant       : !TENANT_ID!
echo Subscription : !SUBSCRIPTION_ID!
echo State backup : !BACKUP!
echo ==========================================
echo Review the plan before terraform apply.

popd
exit /b 0

:error
echo.
echo ERROR: Azure authentication or Terraform failed.
if defined PUSHED popd
exit /b 1
