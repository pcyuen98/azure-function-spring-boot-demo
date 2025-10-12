# ☁️ Azure Function App Demo Application

<p align="center">
  <img src="/pic/azure.png" alt="Azure Logo" width="460" height="300">
</p>

This demo showcases how to run **Azure Functions** efficiently while keeping costs predictable and low.  
It integrates **Azure Functions**, **Azure Storage**, and **Azure Resource Management (ARM)**, all deployed through **Terraform**.

---

## 🧩 Project Structure

The application consists of **Three main components**:

### **1. Function-Util**
A shared utility module that contains common classes and libraries.  
This promotes **code reuse** and **clean separation of concerns** across projects.

---

### **2. Function-Resource-Logging**
The **core Azure Function App**, demonstrating the following capabilities:

#### ⚠️ Cost Considerations for Azure Functions

Azure Functions offer a **free monthly usage grant**:

| Metric | Free Tier | Cost Beyond Free |
|---------|------------|------------------|
| Execution Time | 400,000 GB-s | $0.000016/GB-s |
| Total Executions | 1 million | $0.20 per additional million |

📌 **Important Notes:**
- A **Storage Account** is automatically created with each Function App.  
  This **is not covered** by the free grant — standard storage/networking charges apply.
- **Application Insights** is **enabled by default** and may cause **unexpected costs** if left unchecked.
  - **Reference:** [Azure Functions Pricing](https://azure.microsoft.com/en-us/pricing/details/functions/)  
  - **Example:** [Reddit Case – Unexpected Insight Costs](https://www.reddit.com/r/AZURE/comments/en664s/how_azure_application_insights_cost_our_company/)

#### 💡 Our Approach
To prevent unexpected billing:
- **Application Insights is disabled by default.**  
- **Azure Table Storage** is used for logging instead.

#### 🧱 Terraform Deployment
Demonstrates full **Infrastructure-as-Code (IaC)** provisioning of Azure resources via Terraform.

#### 📂 Azure Storage Integration
Traditional Spring Boot logging writes to `application.log` within the server filesystem.  
However, **Azure Functions** are **serverless** — instances are ephemeral, and files are lost on cold starts.

✅ This project stores logs persistently in **Azure Storage (e.g., Table Storage)** for reliability and traceability.

#### ⚙️ Azure Resource Management
Demonstrates interaction with **Azure Resource Manager (ARM)** to control and monitor Azure services dynamically.

---

### **3. Function-Costing**
A separate Azure Function project designed to **monitor and control resource costs**.

#### ✉️ Costing Notification via Email
Sends detailed **daily or hourly cost reports** via email using **EngageLab API**.

<p align="center">
  <img src="/pic/Costing-Email.jpeg" alt="Azure Costing Email Example" width="460" height="800">
</p>

#### 🔌 Auto Function Shutdown (Beyond Portal Capability)
Automatically **shuts down a function** when cost or API usage exceeds configurable thresholds.  
*(This feature is not available through the Azure Portal by default.)*

---

## 🚀 Getting Started

To start using this project:

1. **Clone the repository**
   ```bash
   git clone https://github.com/pcyuen98/azure-function-spring-boot-demo.git
   ```

2. **Configure your Azure environment**
   Each subproject (`Function-Resource-Logging`, `Function-Costing`, etc.) includes its own setup guide and `application.properties` file.

3. **Build and deploy with Maven + Terraform**
   Follow instructions in the respective subproject README files.

---

## 📘 Additional Resources

🔗 **Function Resource Logging:**  
[https://github.com/pcyuen98/azure-function-spring-boot-demo/tree/main/Function-Resource-Logging](https://github.com/pcyuen98/azure-function-spring-boot-demo/tree/main/Function-Resource-Logging)

🔗 **Function Costing:**  
[https://github.com/pcyuen98/azure-function-spring-boot-demo/tree/main/Function-Costing](https://github.com/pcyuen98/azure-function-spring-boot-demo/tree/main/Function-Costing)
