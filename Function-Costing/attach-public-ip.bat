@echo on
setlocal
set "RG=java-cost-function-resource-group77705"
set "VM=vm-microseg-77705"
set "PIP=pip-microseg-77705"
echo.
echo === Checking VM ===
az vm show -g "%java-cost-function-resource-group77705%" -n "%vm-microseg-77705%" -o table
echo === Finding NIC ===
set "NIC_ID="
for /f "delims=" %%A in ('az vm nic list -g "%java-cost-function-resource-group77705%" --vm-name "%vm-microseg-77705%" --query "[0].id" -o tsv') do set "NIC_ID=%%A"
if not defined NIC_ID goto ERROR
echo NIC ID: %NIC_ID%
set "IPCONFIG="
for /f "delims=" %%A in ('az network nic show --ids "%NIC_ID%" --query "ipConfigurations[0].name" -o tsv') do set "IPCONFIG=%%A"
if not defined IPCONFIG goto ERROR
echo IP Configuration: %IPCONFIG%
az network public-ip show -g "%java-cost-function-resource-group77705%" -n "%pip-microseg-77705%" -o table
if errorlevel 1 goto ERROR
az network nic ip-config update --ids "%NIC_ID%" --name "%IPCONFIG%" --public-ip-address "%pip-microseg-77705%"
if errorlevel 1 goto ERROR
az network public-ip show -g "%java-cost-function-resource-group77705%" -n "%pip-microseg-77705%" --query ipAddress -o tsv
echo SUCCESS
goto END
:ERROR
echo ERROR: Operation failed.
:END
pause
endlocal
