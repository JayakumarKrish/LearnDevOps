**Stage - Deploy app using CLI in GKE**
  - For communicating AWS cli with EC2 instance we need to create secrectKey and accessKey. 
    provider  "aws" {
      region ="us-east-1"
      access_key =  var.accessKey
      secret_key = var.secrectKey
    }
  - To create access and secrectkey - https://www.msp360.com/resources/blog/how-to-find-your-aws-access-key-id-and-secret-access-key/
  - Create IAM role with **AmazonSSMManagedInstanceCore** permission and add it in resource block
    **iam_instance_profile = "cicdIAMRole"**
  - If we need to pass name of the instance add tag like this
    tags = {
      Name = "lf-test-instance"
    }
    
