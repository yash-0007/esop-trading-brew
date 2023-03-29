variable "ami_id" {
  default = "ami-00c39f71452c08778"
}

variable "instance_type" {
  default = "t2.micro"
}

variable "associate_public_ip_address" {
  default = true
}

variable "tags" {
  default = {
    Name = "gurukul_esoptrading_vinayak"
  }
}

variable "region" {
  default = "us-east-1"
}

variable "key_name" {
  default = "gurukul-vinayak"
}

variable "key" {
  type    = string
  default = ""
}

variable "tf_state_bucket" {
  default = "gurukul-vinayak"
}

variable "tf_state_key" {
  default = "terraform.tfstate"
}