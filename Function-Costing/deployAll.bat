
@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" (
    echo Usage: %~nx0 RESOURCE_GROUP_NAME
    echo Example: %~nx0 java-test-function-resource-group77703
    exit /b 1
)

cd /d "%~dp0" || exit /b 1

set "RESOURCE_GROUP=%~1"

echo Checking resource group: !RESOURCE_GROUP!

set "EXISTS="
for /f %%A in ('az group exists --name "!RESOURCE_GROUP!" -o tsv') do set "EXISTS=%%A"

if not defined EXISTS (
    echo ERROR: Unable to check resource group.
    exit /b 1
)

if /I "!EXISTS!"=="true" (
    echo Deleting resource group: !RESOURCE_GROUP!
    call az group delete --name "!RESOURCE_GROUP!" --yes

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

pushd terraform || exit /b 1

if exist ".terraform" rmdir /s /q ".terraform"
if exist ".terraform.lock.hcl" del /f /q ".terraform.lock.hcl"
if exist "terraform.tfstate" del /f /q "terraform.tfstate"
if exist "terraform.tfstate.backup" del /f /q "terraform.tfstate.backup"
if exist "variables.tf.bak" del /f /q "variables.tf.bak"

call terraform init

if errorlevel 1 (
    echo ERROR: Terraform init failed. Stopping.
    popd
    exit /b 1
)

echo ========================================
echo STEP 3: Terraform Plan
echo ========================================

call terraform plan -out=plan.tfplan

if errorlevel 1 (
    echo ERROR: Terraform plan failed. Stopping.
    popd
    exit /b 1
)

echo ========================================
echo STEP 4: Terraform Apply
echo ========================================

call terraform apply plan.tfplan

if errorlevel 1 (
    echo ERROR: Terraform apply failed. Stopping.
    popd
    exit /b 1
)

popd

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

set "FUNCTION_APP="

for /f "usebackq delims=" %%A in (`powershell.exe -NoProfile -Command "[xml]$p=Get-Content -LiteralPath 'pom.xml'; $p.project.properties.functionAppName"`) do set "FUNCTION_APP=%%A"

if not defined FUNCTION_APP (
    echo ERROR: Could not read functionAppName from pom.xml.
    exit /b 1
)

set "URL=https://!FUNCTION_APP!.azurewebsites.net/api/gethistory"

echo URL: !URL!

rem Strict 60-second deadline for health check.

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$url=$env:URL; $end=(Get-Date).AddSeconds(60); $i=0; do { $i++; try { $r=Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5; $status=[int]$r.StatusCode } catch { $status=0; if ($_.Exception.Response) { $status=[int]$_.Exception.Response.StatusCode } }; Write-Host ('Attempt '+$i+' - HTTP '+$status); if ($status -eq 200) { Write-Host 'SUCCESS: Azure Function is responding.'; exit 0 }; $remaining=($end-(Get-Date)).TotalSeconds; if ($remaining -gt 0) { Start-Sleep -Seconds ([Math]::Min(5,$remaining)) } } while ((Get-Date) -lt $end); Write-Host 'ERROR: Azure Function did not return HTTP 200.'; exit 1"

exit /b %ERRORLEVEL%
