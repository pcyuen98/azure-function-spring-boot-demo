
@echo off
setlocal

if "%~1"=="" (
    echo Usage: %~nx0 RESOURCE_GROUP_NAME
    exit /b 1
)

set "RESOURCE_GROUP=%~1"

echo Resource Group: %RESOURCE_GROUP%
echo WARNING: This will permanently delete the resource group and its resources.

choice /C YN /M "Continue with deletion"
if errorlevel 2 exit /b 0

az group delete --name "%RESOURCE_GROUP%" --yes
if errorlevel 1 (
    echo ERROR: Deletion failed.
    exit /b 1
)

echo Checking resource group...
az group exists --name "%RESOURCE_GROUP%"

endlocal
