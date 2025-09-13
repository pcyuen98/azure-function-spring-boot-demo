# Azure Function App Demo Application

<p align="center">
<img src="/pic/azure.png" alt="Azure Logo" width="460" height="300">
</p>

This ensures lower and more predictable costs while running Azure Functions.

This repository contains a demonstration application built on the Azure cloud platform. The project is designed to showcase the integration of various Azure services, with a specific focus on **Azure Functions**, **Azure Storage**, and **Azure Resource Management** (ARM) through **Terraform** deployments.

The application is structured into two main components:

* **Function-Util**: A shared utility project that contains common code and libraries used by the main application. This promotes code reuse and maintains a clean separation of concerns.

* **Function-Resource-Logging**: The core Azure Function App. This project highlights the following capabilities:

  * **Terraform Deployment**: Demonstrates a complete infrastructure-as-code workflow for provisioning Azure resources.

  * **Azure Storage Usage**: In traditional Spring Booot @Slf4j, the file stored inside the server say /log/application.log but Azure Function app serverless will destroy the instance and the files after a cold start. In order to store this, Azure has it own storage. This demo app will demostrate to interact with Azure Storage services to store the log file(e.g., Azure Table Storage) for data persistence. 

  * **Azure Resource Management**: Showcases how to use Azure Resource Manager to manage and interact with other Azure services from within the function.

* **⚠️ Cost Considerations on Azure Functions**:

Azure Functions includes a **free monthly grant**:

- **Execution Time**: 400,000 GB-s free per month  
  ($0.000016/GB-s thereafter, pay-as-you-go)  
- **Total Executions**: 1 million executions free per month  
  ($0.20 per additional million executions)

📌 **Important Notes:**
- A **Storage Account** is created by default with each Functions app.  
  This storage account is **not included** in the free grant. Standard storage and networking charges apply.  
- By default, **Application Insights** is enabled, which can result in unexpected high costs if not carefully managed.  
  - Reference: [Azure Pricing – Functions](https://azure.microsoft.com/en-us/pricing/details/functions/)  
  - Real-world case: [Reddit discussion](https://www.reddit.com/r/AZURE/comments/en664s/how_azure_application_insights_cost_our_company/)

* ** Our Approach**:
To avoid unexpected charges:
- **Application Insights is disabled by default** in this project.  
- **Table Storage** is used for logging instead of Application Insights.  

### Getting Started

To get started with this project, you will need to clone the repository and configure your Azure environment. Detailed instructions for each component can be found within their respective subdirectories.

This README is designed to give a high-level overview. Feel free to explore the code to see how these technologies are implemented.

# Check out here for more info
https://github.com/pcyuen98/azure-function-spring-boot-demo/tree/main/Function-Resource-Logging



