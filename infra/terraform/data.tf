data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "nextime-food-state-bucket"
    key    = "infra-core/infra.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "sqs" {
  backend = "s3"
  config = {
    bucket = "nextime-food-state-bucket"
    key    = "sqs/infra.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "kubernetes" {
  backend = "s3"
  config = {
    bucket = "nextime-food-state-bucket"
    key    = "infra-kubernetes/cluster.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "s3" {
  backend = "s3"
  config = {
    bucket = "nextime-food-state-bucket"
    key    = "s3/infra.tfstate"
    region = "us-east-1"
  }
}
