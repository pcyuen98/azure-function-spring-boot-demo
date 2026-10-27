# ☁️ Azure Resource and Costing Function

## 💼 Useful Services of the Resource and Costing System

-   **Costing Email Timer**\
    Sends hourly emails containing daily costing and API usage count per
    hour.

-   **Shutdown Timer**\
    Runs every 10 minutes; if the function cost or API count exceeds the
    threshold, it triggers an automatic shutdown service.

------------------------------------------------------------------------

## 🚀 How to Run

Follow the steps below to set up, build, and deploy the Azure Resource
and Costing Function.

------------------------------------------------------------------------

### 1️⃣ Setup the Environment

Set up all required environment variables before building or running the
project.

``` bash
# Resource ID to be monitored (shutdown invoked if over limit)
export AZURE_FUNCTION_RESOURCE_ID=/subscriptions/<sub-id>/resourceGroups/<resource-group>/providers/Microsoft.Web/sites/<function-name>

# Email recipient for costing information
export EMAIL_RECEIPIENT=youremail@example.com

# Obtain free email service credentials from https://www.engagelab.com/
export ENGAGELAB_API_USER=<your-engagelab-user>
export ENGAGELAB_API_KEY=<your-engagelab-key>
```

> 💡 **Tip (Windows Users):** Replace `export` with `set` if using
> Command Prompt.

------------------------------------------------------------------------

### 2️⃣ Build the Project

Login to Azure and build the entire project using Maven:

``` bash
az login
mvn clean install
```

------------------------------------------------------------------------

### 3️⃣ Deploy Infrastructure with Terraform

Navigate to the Terraform directory and deploy your Azure
infrastructure:

``` bash
cd terraform
terraform init
terraform plan -out plan.tfplan
terraform apply plan.tfplan
```

------------------------------------------------------------------------

### 4️⃣ Run the Function App Locally

Go back to the `Function-Costing` directory and run the function
locally:

``` bash
cd ..
mvn clean package azure-functions:run
```

------------------------------------------------------------------------

### 5️⃣ Deploy the Function App to Azure

Once tested locally, update `application.properties` with your
environment variables from **Step 1**, then deploy:

``` bash
mvn clean package azure-functions:deploy
```

> ✅ **Note:**\
> After successful deployment, a list of the Function RESTful API
> endpoints will be displayed in the console output.


### create an application.properties file under src/main/resource/application.properties

azure.resource.group=java-cost-function-resource-group777
azure.storage.name=coststorage777
azure.function.name=java-cost-function-777

# Resource ID for the deployed Azure Function
# Provide resource ID as format below
# /subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{providerNamespace}/{resourceType}/{resourceName}
# i.e. /subscriptions/xxxxxx-4d66-4bee-a612-e66b89fbf8e9/resourceGroups/java-cost-function-resource-group777/providers/Microsoft.Web/sites/java-cost-function-777

azure.function.resource-id=${AZURE_FUNCTION_RESOURCE_ID}

# Email recipient for notifications
azure.function.receipient.email=${EMAIL_RECEIPIENT}

azure.function.limit.cost=10
azure.function.limit.api=200

#Obtain your api key and user from https://www.engagelab.com/ to send the email
belisty.engagelab.api.user=${ENGAGELAB_API_USER}
belisty.engagelab.api.key=${ENGAGELAB_API_KEY}

belisty.engagelab.api.base-url=https://email.api.engagelab.cc/v1/mail/send
belisty.engagelab.api.from-email=EngageLab@vqoricrahxhefdvkdmtcdoxbbaoldcoo.send.engagelab.email

