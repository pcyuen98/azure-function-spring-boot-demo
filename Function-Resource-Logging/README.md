## Useful Services of the Resource and Logging Services

- **Terraform Setting**: It only deployed the basic function of the function app. Log Analytic is disabled as it may cost tremendous charging if not careful. This is mentioned in this Reddit thread: https://www.reddit.com/r/AZURE/comments/en664s/how_azure_application_insights_cost_our_company/.

- **Retention**: Retention of the storage is limited to 1 day. Azure charges based on quantity used, so the lesser data retention, the lesser the possible charges.

- **Logging**: Logging is stored inside an Azure storage table instead of analytic storage managed by AI. Auto Management by Azure AI means Auto Charging by Microsoft.

- **Security**: Automatically configures the security identity to manage the Azure Function App Resources.

- **Storage**: Automatically configure and deploy a Azure storage with configurable naming convention. 

- **Resource Manager**: Manage resources as a Spring Service such as instance ID, storage connection ID, stop/start services from application context

- **Shutdown Service**: A shutdown service is available and is useful to turn it off when not using it.

## How to Run

To run and deploy this application, follow these steps:

### 1. Build the Project

First, build the entire project using Maven:

```bash
mvn clean install
```

### 2. Deploy Infrastructure with Terraform

Next, navigate to the `Function-Resource-Logging/terraform` directory and deploy your Azure infrastructure.

```bash
cd terraform
terraform init
terraform plan -out plan.tfplan
terraform apply plan.tfplan
```

### 3. Deploy the Function App

Finally, navigate back to the `Function-Resource-Logging` directory and deploy the function app to Azure:

```bash
cd ..
mvn clean package azure-functions:deploy
```

### Note: If deployment successful, a list of the Function Restful API will be displayed.