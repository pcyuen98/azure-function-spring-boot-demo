terraform {
  required_version = ">= 1.6.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

provider "azurerm" {
  features {}
}

# Resource group to host everything
resource "azurerm_resource_group" "rg" {
  name     = var.resource_group_name
  location = var.location
}

# Generate unique suffix for storage account name
resource "random_string" "suffix" {
  length  = 6
  upper   = false
  lower   = true
  numeric = true
  special = false
}

locals {
  storage_account_name_effective = coalesce(
    var.storage_account_name,
    lower(replace("${var.name_prefix}sa${random_string.suffix.result}", "/[^a-z0-9]/", ""))
  )
}

# Deploy the ARM template
resource "azurerm_resource_group_template_deployment" "arm_deploy" {
  name                = "${var.name_prefix}-armdeploy"
  resource_group_name = azurerm_resource_group.rg.name
  deployment_mode     = "Incremental"

  template_content = file("${path.module}/template-test.json")

  parameters_content = jsonencode({
    location = {
      value = var.location
    }
    storageAccountName = {
      value = local.storage_account_name_effective
    }
    servicePlanName = {
      value = var.app_service_plan_name
    }
    functionAppName = {
      value = var.function_app_name
    }
  })

  timeouts {
    create = "30m"
    update = "30m"
  }
}

# --------------------
# Data Sources
# --------------------

# Get current subscription
data "azurerm_subscription" "current" {}

# Get deployed Function App (from ARM template)
data "azurerm_linux_function_app" "func" {
  name                = var.function_app_name
  resource_group_name = azurerm_resource_group.rg.name

  depends_on = [
    azurerm_resource_group_template_deployment.arm_deploy
  ]
}

# --------------------
# Role Assignment
# --------------------

# Assign Owner role to Function App system-assigned identity at subscription level
resource "azurerm_role_assignment" "function_owner" {
  scope                = data.azurerm_subscription.current.id
  role_definition_name = "Owner"
  principal_id         = data.azurerm_linux_function_app.func.identity[0].principal_id

  depends_on = [
    data.azurerm_linux_function_app.func
  ]
}

# --------------------
# Outputs
# --------------------

output "function_app_name" {
  value = var.function_app_name
}

output "storage_account_name" {
  value = local.storage_account_name_effective
}
