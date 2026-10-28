
@echo off
setlocal

if "%~2"=="" (
    echo Usage: %~nx0 OLD_VALUE NEW_VALUE
    echo Example: %~nx0 77701 77703
    exit /b 1
)

set "OLD=%~1"
set "NEW=%~2"
set "ROOT=%~dp0"
set "PSFILE=%TEMP%\updateresource_%RANDOM%.ps1"

echo Replacing %OLD% with %NEW%
echo.

> "%PSFILE%" echo $root = $env:ROOT
>> "%PSFILE%" echo $old = $env:OLD
>> "%PSFILE%" echo $new = $env:NEW
>> "%PSFILE%" echo $files = @('pom.xml','src\main\resources\application.properties','terraform\variables.tf')
>> "%PSFILE%" echo $utf8 = New-Object System.Text.UTF8Encoding($false)
>> "%PSFILE%" echo foreach ($file in $files) {
>> "%PSFILE%" echo   $path = Join-Path $root $file
>> "%PSFILE%" echo   if (-not (Test-Path -LiteralPath $path)) { Write-Host "MISSING: $file"; continue }
>> "%PSFILE%" echo   $content = [System.IO.File]::ReadAllText($path)
>> "%PSFILE%" echo   if ($content.Contains($old)) {
>> "%PSFILE%" echo     Copy-Item -LiteralPath $path -Destination ($path + '.bak') -Force
>> "%PSFILE%" echo     $updated = $content.Replace($old,$new)
>> "%PSFILE%" echo     [System.IO.File]::WriteAllText($path,$updated,$utf8)
>> "%PSFILE%" echo     Write-Host "UPDATED: $file"
>> "%PSFILE%" echo   } else { Write-Host "NO MATCH: $file" }
>> "%PSFILE%" echo }

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%PSFILE%"
set "RESULT=%ERRORLEVEL%"

del "%PSFILE%" >nul 2>&1

if not "%RESULT%"=="0" exit /b %RESULT%

echo.
echo Finished. Original files backed up as .bak
endlocal
