@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem Azure Cost Management quota monitor. Requires Azure CLI and PowerShell.
rem Usage: monitor-azure-cost-quota.bat [interval_seconds]
set "INTERVAL=30"
if not "%~1"=="" set "INTERVAL=%~1"
echo(%INTERVAL%| findstr /r "^[1-9][0-9]*$" >nul
if errorlevel 1 (
    echo ERROR: Interval must be a positive whole number of seconds.
    exit /b 1
)

where az >nul 2>&1
if errorlevel 1 (
    echo ERROR: Azure CLI not found in PATH.
    exit /b 1
)

for /f "usebackq delims=" %%S in (`az account show --query id -o tsv 2^>nul`) do set "SUBSCRIPTION_ID=%%S"
if not defined SUBSCRIPTION_ID (
    echo ERROR: No active Azure subscription. Run az login and az account set.
    exit /b 1
)

set "URL=https://management.azure.com/subscriptions/%SUBSCRIPTION_ID%/providers/Microsoft.CostManagement/query?api-version=2023-11-01"
set "BODY={\"type\":\"ActualCost\",\"timeframe\":\"MonthToDate\",\"dataset\":{\"granularity\":\"None\",\"aggregation\":{\"totalCost\":{\"name\":\"Cost\",\"function\":\"Sum\"}}}}"
set "LOGFILE=%TEMP%\azure-cost-quota-%RANDOM%-%RANDOM%.log"

echo Subscription: %SUBSCRIPTION_ID%
echo Interval: %INTERVAL% seconds
 echo Press Ctrl+C to stop.
echo.

:LOOP
call az rest --method post --url "%URL%" --headers "Content-Type=application/json" --body "%BODY%" --verbose > "%LOGFILE%" 2>&1
set "AZ_EXIT=%ERRORLEVEL%"
echo ------------------------------------------------------------
echo %DATE% %TIME%  ^|  Subscription: %SUBSCRIPTION_ID%
set "QUOTA_LOG=%LOGFILE%"
powershell -NoProfile -Command "$t=Get-Content -LiteralPath $env:QUOTA_LOG -Raw; $status=[regex]::Match($t,'Response status:\s*(\d+)').Groups[1].Value; if(!$status){$status='unknown'}; $client=[regex]::Match($t,'costmanagement-clienttype-requests''?\s*:\s*''?([^''\r\n]+)', 'IgnoreCase').Groups[1].Value; $qpu=[regex]::Match($t,'costmanagement-qpu-remaining''?\s*:\s*''?([^''\r\n]+)', 'IgnoreCase').Groups[1].Value; $retry=[regex]::Match($t,'costmanagement-clienttype-retry-after''?\s*:\s*''?([^''\r\n]+)', 'IgnoreCase').Groups[1].Value; if(!$client){$client='not provided'}; if(!$qpu){$qpu='not provided'}; Write-Host ('HTTP status: '+$status); Write-Host ('Client-type remaining: '+$client); if($qpu -ne 'not provided'){foreach($k in @('QueriesPer10Sec','QueriesPerMin','QueriesPerHour')){$m=[regex]::Match($qpu,[regex]::Escape($k)+':(\d+)'); $v=if($m.Success){$m.Groups[1].Value}else{'not provided'}; Write-Host ($k+' remaining: '+$v)}}else{Write-Host 'QPU remaining: not provided'}; if($retry){Write-Host ('Client-type retry-after: '+$retry+' seconds')}; if($status -eq 'unknown'){Write-Host 'No HTTP response status found; check Azure CLI login/connectivity.'}"
if not "%AZ_EXIT%"=="0" echo Azure CLI exit code: %AZ_EXIT%
echo.
rem Pause between requests to avoid exhausting the API quota.
timeout /t %INTERVAL% /nobreak >nul
goto LOOP
