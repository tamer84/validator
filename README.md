# Validator

Validates products

## Code

This project contains a single entrypoint (EventHandler class), responsible for handling all `ValidatableEvents` .

### [EventHandler](.../lambda/kotlin/EventHandler.kt)

For this case we use the generic [RequestStreamHandler](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html) which allow a safer deserialization of the payload (as mentioned before, AWS Jackson deserialization is pretty limited).

This handler will receive both an InputStream and an OutputStream, the request content will be read from the input stream. The output stream is usually not used since it is not needed to send any data back, but for testing purposes success message is written on it. 


## Resources

This application uses [Terraform](https://www.terraform.io/) to create the necessary cloud resources.

The file [deploy.tf](deploy.tf) contains the description of all necessary resources with the necessary explanations.

Tango uses [Terraform Workspaces](https://www.terraform.io/docs/state/workspaces.html) to control multiple environments (dev, test, prod, etc). Follow below a simple way of using terraform commands:

```shell script
# First, initialize terraform, this will download the necessary remote states and modules used
$ terraform init

# Now you need to select the proper workspace, which represents an environment
$ terraform workspace select dev # or test, or int, or prod, or etc...

# The next step is to verify if the cloud resources are any different from your local files, terraform plan will show you the possible differences
$ terraform plan

# After analyzing the plan, you can apply this changes. Please be careful, this process should optimally be done exclusively from CICD
$ terraform apply  
```
