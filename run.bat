mvn clean install

cd Function-Resource-Logging/terraform
terraform init
terraform plan -out plan.tfplan
terraform apply plan.tfplan

cd ..
mvn clean package azure-functions:deploy 