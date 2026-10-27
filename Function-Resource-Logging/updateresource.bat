
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

echo Replacing %OLD% with %NEW%
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$root=$env:ROOT; $old=$env:OLD; $new=$env:NEW;" ^
  "$files=@('pom.xml','src\main\resources\application.properties','terraform\variables.tf');" ^
  "$utf8=New-Object System.Text.UTF8Encoding($false);" ^
  "foreach($file in $files) {" ^
  "  $path=Join-Path $root $file;" ^
  "  if(!(Test-Path -LiteralPath $path)) { Write-Host ('MISSING: '+$file); continue }" ^
  "  $content=[System.IO.File]::ReadAllText($path);" ^
  "  if($content.Contains($old)) {" ^
  "    Copy-Item -LiteralPath $path -Destination ($path+'.bak') -Force;" ^
  "    $updated=$content.Replace($old,$new);" ^
  "    [System.IO.File]::WriteAllText($path,$updated,$utf8);" ^
  "    Write-Host ('UPDATED: '+$file)" ^
  "  } else { Write-Host ('NO MATCH: '+$file) }" ^
  "}"

if errorlevel 1 exit /b 1

echo.
echo Finished. Original files backed up as .bak
endlocal
