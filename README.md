# Azure Demo Application

<p align="center">
<img src="/pic/azure.png" alt="Azure Logo" width="460" height="300">
</p>

## Overview

This repository contains a demonstration application built on the Azure cloud platform. The project is designed to showcase the integration of various Azure services, with a specific focus on **Azure Functions**, **Azure Storage**, and **Azure Resource Management** (ARM) through **Terraform** deployments.

The application is structured into two main components:

* **Function-Util**: A shared utility project that contains common code and libraries used by the main application. This promotes code reuse and maintains a clean separation of concerns.

* **Function-Resource-Logging**: The core Azure Function App. This project highlights the following capabilities:

  * **Terraform Deployment**: Demonstrates a complete infrastructure-as-code workflow for provisioning Azure resources.

  * **Azure Storage Usage**: Illustrates how to interact with Azure Storage services (e.g., Azure Table Storage) for data persistence.

  * **Azure Resource Management**: Showcases how to use Azure Resource Manager to manage and interact with other Azure services from within the function.

### Getting Started

To get started with this project, you will need to clone the repository and configure your Azure environment. Detailed instructions for each component can be found within their respective subdirectories.

This README is designed to give a high-level overview. Feel free to explore the code to see how these technologies are implemented.

## Other Useful Services of the Demo App

- **Terraform Setting**: It only deployed the basic function of the function app. Log Analytic is disabled as it may cost tremendous charging if not careful. This is mentioned in this Reddit thread: https://www.reddit.com/r/AZURE/comments/en664s/how_azure_application_insights_cost_our_company/.

- **Retention**: Retention of the storage is limited to 1 day. Azure charges based on quantity used, so the lesser data retention, the lesser the possible charges.

- **Logging**: Logging is stored inside an Azure storage table instead of analytic storage managed by AI.

- **Security**: Automatically configures the security identity to manage the Azure Function App Resources.

- **Shutdown Service**: A shutdown service is available and is useful to turn it off when not using it.

## How to Run

### Build the project
```bash
mvn clean install

cd Function-Resource-Logging/terraform
terraform init
terraform plan -out plan.tfplan
terraform apply plan.tfplan

cd ..
mvn clean package azure-functions:deploy```

### Note: If deployment successful, a list of the Function Restful API will be displayed.