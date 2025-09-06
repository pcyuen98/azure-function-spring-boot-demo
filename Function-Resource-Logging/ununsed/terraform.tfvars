resource_group_name  = "java-test-function-resource-group777"
location             = "westeurope"

name_prefix          = "jtf777"
action_group_name    = "Application-Insights-Smart-Detection"
app_insights_name    = "java-test-function-777"
app_service_plan_name= "java-test-function-service-plan-777"
function_app_name    = "java-test-function-777"

# Leave empty to auto-generate a unique SA name, or supply your own:
# storage_account_name = "jtf777sa123abc"

# If you already have a workspace, set:
# create_workspace      = false
# workspace_resource_id = "/subscriptions/<subId>/resourceGroups/<rg>/providers/Microsoft.OperationalInsights/workspaces/<name>"
