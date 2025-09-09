variable "resource_group_name" {
  description = "Resource group to create/use"
  type        = string
  default     = "java-test-function-resource-group777"
}

variable "location" {
  description = "Azure region of the resource group (use westeurope to match your template)"
  type        = string
  default     = "westeurope"
}

variable "name_prefix" {
  description = "Short name prefix for generated resources"
  type        = string
  default     = "jtf777"
}

# ARM parameters
variable "action_group_name" {
  description = "Name for microsoft.insights/actionGroups"
  type        = string
  default     = "Application-Insights-Smart-Detection"
}

variable "app_insights_name" {
  description = "Application Insights component name"
  type        = string
  default     = "java-test-function-777"
}

variable "storage_account_name_effective" {
  description = "storage_account_name_effective component name"
  type        = string
  default     = "storage-test-function-777"
}

variable "app_service_plan_name" {
  description = "Serverfarm (Functions Consumption Y1)"
  type        = string
  default     = "java-test-function-service-plan-777"
}

variable "function_app_name" {
  description = "Function App site name"
  type        = string
  default     = "java-test-function-777"
}

variable "storage_account_name" {
  description = "Storage account name (leave empty to auto-generate)"
  type        = string
  default     = "javastorage777"
  validation {
    condition     = length(var.storage_account_name) == 0 || (length(var.storage_account_name) >= 3 && length(var.storage_account_name) <= 24 && can(regex("^[a-z0-9]+$", var.storage_account_name)))
    error_message = "storage_account_name must be 3-24 lowercase alphanumeric characters."
  }
}

# Workspace handling
variable "create_workspace" {
  description = "Create a new Log Analytics workspace? If false, provide workspace_resource_id."
  type        = bool
  default     = true
}

variable "workspace_resource_id" {
  description = "Existing Log Analytics workspace resource ID (used when create_workspace=false)"
  type        = string
  default     = ""
}
