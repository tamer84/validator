# ========================================
# Variables
# ========================================
variable "application_version" {
  type = string
}

variable "application_name" {
  type    = string
  default = "validator"
}

variable "aws_region" {
  type    = string
  default = "eu-central-1"
}

# ========================================
# Initialization
# ========================================
terraform {
  // Declares where terraform stores the application state
  backend "s3" {
    encrypt        = "true"
    bucket         = "tango-terraform"
    key            = "resources/validator/tfstate.tf"
    region         = "eu-central-1"
    dynamodb_table = "terraform"
  }
}

provider "aws" {
  // Use the AWS provider from terraform https://www.terraform.io/docs/providers/aws/index.html
  region  = "eu-central-1"
}


provider "github" {
  token        = data.terraform_remote_state.account_resources.outputs.github_access_token
}

data "terraform_remote_state" "account_resources" {
  // Imports the account resources to use the shared information
  backend = "s3"
  config = {
    encrypt = "true"
    bucket  = "tango-terraform"
    key     = "account_resources/tfstate.tf"
    region  = "eu-central-1"
  }
  workspace = "default"
}

data "terraform_remote_state" "environment_resources" {
  // Imports the environment specific resources to use the shared information
  backend = "s3"
  config = {
    encrypt = "true"
    bucket  = "tango-terraform"
    key     = "environment_resources/tfstate.tf"
    region  = "eu-central-1"
  }
  workspace = terraform.workspace
}

data "terraform_remote_state" "terraform_build_image_resources" {
  backend = "s3"
  config = {
    encrypt = "true"
    bucket  = "tango-terraform"
    key     = "resources/terraform-build-image/tfstate.tf"
    region  = "eu-central-1"
  }
  workspace = terraform.workspace
}

data "terraform_remote_state" "aggregator_resources" {
  backend = "s3"
  config = {
    encrypt = "true"
    bucket  = "tango-terraform"
    key     = "resources/aggregator/tfstate.tf"
    region  = "eu-central-1"
  }
  workspace = terraform.workspace
}

data "aws_caller_identity" "current" {}

# ========================================
# Locals
# ========================================
locals {
  code   = "target/${var.application_name}-${var.application_version}.jar"
  ruleId = 1
  rule = {
    Name : "${terraform.workspace}_${var.application_name}",
    EventPattern : jsonencode({
      "detail-type" : [
        "ValidatableEvent"
      ],
      "detail" : {
        "productType" : [
          "ICE_CREAM"
        ]
      }
    }),
    State : "ENABLED",
    Description : "Subscribe to the specific events above",
    EventBusName : data.terraform_remote_state.environment_resources.outputs.eventbus
  }
  cicd_branch = contains(["dev", "test", "int"], terraform.workspace) ? "develop" : "main"
}


# ========================================
# Lambda
# ========================================
resource "aws_lambda_function" "lambda" {
  // Deploys the lambda itself https://www.terraform.io/docs/providers/aws/r/lambda_function.html
  // This deployment is generic, despite of being triggered by the API Gateway or EventBridge,
  //  the trigger is configured at the specific session below
  function_name = "${var.application_name}-${terraform.workspace}"
  handler       = "com.tamer84.tango.product.validator.EventHandler"

filename      = local.code
  runtime       = "java11"
  // For java applications the minimum I would suggest is 512
  memory_size = 512
  //This will create a new version of each deployment, allow simple rollback or AB testing
  publish = true
  // This is what triggers the redeploy, if the code has changed a new lambda version is created
  source_code_hash = filebase64sha256(local.code)
  role             = data.terraform_remote_state.account_resources.outputs.lambda_default_exec_role.arn
  timeout          = 300

  environment {
    // This is how you inject environment variables
    variables = {
      VERSION                  = var.application_version
      ENVIRONMENT              = terraform.workspace
      APPLICATION_NAME         = "${var.application_name}-${terraform.workspace}"
      AGGREGATOR_URL           = "https://${data.terraform_remote_state.aggregator_resources.outputs.route53_record.fqdn}"
      DESTINATION_BUS          = data.terraform_remote_state.environment_resources.outputs.eventbus
      MIN_IMAGE_COUNT_REQUIREMENT = 8
    }
  }

  dead_letter_config {
    target_arn = aws_sqs_queue.dlq.arn
  }

  vpc_config {
    security_group_ids = [data.terraform_remote_state.environment_resources.outputs.group_internal_access.id]
    subnet_ids         = data.terraform_remote_state.environment_resources.outputs.private-subnet.*.id
  }

  tags = {
    Terraform   = "true"
    Environment = terraform.workspace
    Product     = "Tango"
    Name        = "${var.application_name}-${terraform.workspace}"
    Application = var.application_name
    Role        = "validation"
    Public      = "false"
    Core        = "true"
  }
}

resource "aws_sqs_queue" "dlq" {
  name                        = "${var.application_name}-${terraform.workspace}"
  fifo_queue                  = false
  content_based_deduplication = false
  message_retention_seconds   = 1209600
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "/aws/lambda/${var.application_name}-${terraform.workspace}"
}


resource "aws_lambda_provisioned_concurrency_config" "provisioned_concurrency" {
  count                             = terraform.workspace == "prod" ? 1 : 0
  function_name                     = aws_lambda_function.lambda.function_name
  provisioned_concurrent_executions = 150
  qualifier                         = aws_lambda_function.lambda.version
}


# ========================================
# Subscribe to event bus
# ========================================
module "eventbridge-rule" {
  source = "git::ssh://git@github.com/tamer84/infra.git//modules/eventbridge-rule?ref=develop"

  rule_id    = local.ruleId
  rule       = local.rule
  target_arn = aws_lambda_function.lambda.arn
}
# ========================================
# CodeBuild
# ========================================
module "cicd" {
  source = "git::ssh://git@github.com/tamer84/infra.git//modules/cicd?ref=develop"

  codestar_connection_arn = data.terraform_remote_state.account_resources.outputs.git_codestar_conn.arn

  pipeline_base_configs = {
    "name"        = "${var.application_name}-${terraform.workspace}"
    "bucket_name" = data.terraform_remote_state.environment_resources.outputs.cicd_bucket.id
    "role_arn"    = data.terraform_remote_state.account_resources.outputs.cicd_role.arn
  }

  codebuild_build_stage = {
    "project_name"        = "${var.application_name}-${terraform.workspace}"
    "github_branch"       = local.cicd_branch
    "github_repo"         = "tamer84/${var.application_name}"
    "github_access_token" = data.terraform_remote_state.account_resources.outputs.github_access_token
    "github_certificate"  = "${data.terraform_remote_state.environment_resources.outputs.cicd_bucket.arn}/${data.terraform_remote_state.environment_resources.outputs.github_cert.id}"

    "service_role_arn"   = data.terraform_remote_state.account_resources.outputs.cicd_role.arn
    "cicd_bucket_id"     = data.terraform_remote_state.environment_resources.outputs.cicd_bucket.id
    "vpc_id"             = data.terraform_remote_state.environment_resources.outputs.vpc.id
    "subnets_ids"        = data.terraform_remote_state.environment_resources.outputs.private-subnet.*.id
    "security_group_ids" = [data.terraform_remote_state.environment_resources.outputs.group_internal_access.id]

    "docker_img_url"                   = data.terraform_remote_state.terraform_build_image_resources.outputs.ecr_repository.repository_url
    "docker_img_tag"                   = "latest"
    "docker_img_pull_credentials_type" = "SERVICE_ROLE"
    "buildspec"                        = "./buildspec.yml"
    "env_vars" = [
      {
        name  = "ENVIRONMENT"
        value = terraform.workspace
      }]
  }
}
