
@echo on
setlocal

set "RG=java-cost-function-resource-group77705"
set "VM=vm-microseg-77705"
set "PIP=pip-microseg-77705"

echo.
echo === Checking VM ===
az vm show -g "%RG%" -n "%VM%" -o table

echo.
echo === Finding NIC ===
set "NIC_ID="

for /f "delims=" %%A in ('az vm nic list -g "%RG%" --vm-name "%VM%" --query "[0].id" -o tsv') do set "NIC_ID=%%A"

if not defined NIC_ID goto ERROR

echo NIC ID: %NIC_ID%

echo.
echo === Finding IP configuration ===
set "IPCONFIG="

for /f "delims=" %%A in ('az network nic show --ids "%NIC_ID%" --query "ipConfigurations[0].name" -o tsv') do set "IPCONFIG=%%A"

if not defined IPCONFIG goto ERROR

echo IP Configuration: %IPCONFIG%

echo.
echo === Checking Public IP ===
az network public-ip show -g "%RG%" -n "%PIP%" -o table
if errorlevel 1 goto ERROR

echo.
echo === Attaching Public IP ===
az network nic ip-config update ^
 --ids "%NIC_ID%" ^
 --name "%IPCONFIG%" ^
 --public-ip-address "%PIP%"

if errorlevel 1 goto ERROR

echo.
echo === Public IP Address ===
az network public-ip show ^
 -g "%RG%" ^
 -n "%PIP%" ^
 --query ipAddress -o tsv

echo.
echo SUCCESS
goto END

:ERROR
echo.
echo ERROR: Operation failed.
echo Check VM, NIC, IP configuration and Public IP.

:END
echo.
pause
endlocal
