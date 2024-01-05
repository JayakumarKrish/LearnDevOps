variable "ami" {
  type = string
}
variable "instanceType" {
  type = string
}
variable "keyName" {
  type = string
}
#terraform {
#  required_providers {
#    aws = {
#      source  = "hashicorp/aws"
#      version = "3.76.1"
#    }
#  }
#}
variable "accessKey" {
  type = string
}
variable "secrectKey" {
  type = string
}

provider "aws" {
  region ="us-east-1"
  access_key =  var.accessKey
  secret_key = var.secrectKey
}
resource "aws_instance" "webserver" {
  ami = var.ami
  instance_type = var.instanceType
  key_name = var.keyName
  vpc_security_group_ids = ["sg-0543f6153e6159486"]
  associate_public_ip_address = true
  user_data = "apt-get install openjdk-8-jdk"
  iam_instance_profile = "cicdIAMRole"
  
  tags = {
    Name = "lf-test-instance"
  }
  provisioner "remote-exec" {
    inline = [
          "sudo apt-get update",
	        "sudo apt-get install openjdk-8-jdk -y"
    ]
    connection {
       type        = "ssh"
       user        = "ubuntu"
       private_key = "${file("/home/logicfocus/terraform/cicdInAWS/jai-test-key.pem")}"
       host        = "${self.public_ip}"
    }
  }
  provisioner "file" {
       source      = "/home/logicfocus/jai/cicdtest/build/libs/gradleSampleArt.jar" 
       destination = "/tmp/gradleSampleArt.jar"
       
    connection {
       type        = "ssh"
       user        = "ubuntu"
       private_key = "${file("/home/logicfocus/terraform/cicdInAWS/jai-test-key.pem")}"
       host        = "${self.public_ip}"
    }
  }
}
