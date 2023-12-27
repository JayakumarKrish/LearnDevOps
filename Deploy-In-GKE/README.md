This pipeline deploys the springboot application in **Google Kubernates Engine**.

**Stage: App with Database in GKE**

  - Springboot application with database integration. (**Repository:** https://gitlab.com/jayakumar97/cicdtest.git, **Branch:** 2.0)
  - Step to reproduce : 
    1. Create cluster in Google cloud console
    2. Give necessary roles to service account for to push docker images into Artifact Registry
       - Artifact Registry Administrator
       - Artifact Registry Reader
       - Artifact Registry Repository Administrator
       - Artifact Registry Writer
       - Container Registry Service Agent
       - gce-storageOwner
       - roles/artifactregistry.createOnPushRepoAdmin
       - Service Account Token Creator
       - Storage Admin
       - Storage Object Admin

**Stage: Deploy app using CLI in GKE**

  - This stage will build docker image in local machine and push them into Google Artifact Registry.
  - Creat kubernates cluster
  - Enable following services compute.googleapis.com, container.googleapis.com
  - Deploy gcr image. (us-east1-docker.pkg.dev) 
   
