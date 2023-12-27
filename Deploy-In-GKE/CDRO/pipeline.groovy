pipeline 'Deploy-In-GKE', {
  projectName = 'jai'

  formalParameter 'releaseVersion', defaultValue: '', {
    label = 'Release version'
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  stage 'Build', {
    colorCode = '#289ce1'
    pipelineName = 'Deploy-In-GKE'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task 'Create branch', {
      actualParameter = [
        'commandToRun': '''#echo "====>$[/projects[jai]/credentials[GitlabAccessToken]]/password"
#!/bin/bash

ISBRANCHEXIST=$(curl --header "PRIVATE-TOKEN:$[/myPipeline/gitLabAccessToken]" "https://gitlab.com/api/v4/projects/$[/myPipeline/CICDTestServiceId]/repository/branches/$[releaseVersion]")
if echo "$ISBRANCHEXIST" | grep -q "404 Branch Not Found"; then
    curl --request POST --header "PRIVATE-TOKEN:$[/myPipeline/gitLabAccessToken]" "https://gitlab.com/api/v4/projects/$[/myPipeline/CICDTestServiceId]/repository/branches?branch=$[releaseVersion]&ref=$[/myPipeline/referenceBranch]"
else
    echo "Branch already exists"
fi''',
      ]
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'CI build', {
      actualParameter = [
        'releaseVersion': '$[releaseVersion]',
      ]
      ciConfigurationName = 'jenkins75'
      ciJobFolder = 'Jai/'
      ciJobName = 'GKE'
      subproject = 'jai'
      taskType = 'CI_JOB'
    }
  }

  stage 'App with Database in GKE', {
    colorCode = '#ff7f0e'
    pipelineName = 'Deploy-In-GKE'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task 'Get Deployment and Service File', {
      actualParameter = [
        'artifacts': '*.yaml',
        'build_number': '',
        'config_name': '/projects/jai/pluginConfigurations/JenkinsCICDTestService',
        'job_name': 'GKE',
        'target_directory': '',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Jenkins'
      subprocedure = 'DownloadArtifacts'
      taskType = 'PLUGIN'
    }

    task 'mysql-config', {
      actualParameter = [
        'commandToRun': '''/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters get-credentials cicdtest-cluster --zone us-east1-b --project lftest-407308
cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f mysql-configMap.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'mysql-Secrect', {
      actualParameter = [
        'commandToRun': '''/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters get-credentials cicdtest-cluster --zone us-east1-b --project lftest-407308
cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f mysql-secrets.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'DB Deployment', {
      actualParameter = [
        'commandToRun': '''/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters get-credentials cicdtest-cluster --zone us-east1-b --project lftest-407308
cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f db-deployment.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'waiting for approval', {
      notificationEnabled = '1'
      notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
      subproject = 'jai'
      taskType = 'MANUAL'
      useApproverAcl = '1'
      approver = [
        'admin',
      ]
    }

    task 'App Deployment', {
      actualParameter = [
        'commandToRun': '''/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters get-credentials cicdtest-cluster --zone us-east1-b --project lftest-407308
cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f app-Deployment.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }
  }

  stage 'Deploy app using CLI in GKE', {
    colorCode = '#2ca02c'
    pipelineName = 'Deploy-In-GKE'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task 'Build docker image', {
      actualParameter = [
        'commandToRun': '''cd /home/logicfocus/jai/cicdtest
ls
echo "lf@2022" | sudo -S docker build -t cicdtest:2.0 .''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Push docker image to GCR', {
      actualParameter = [
        'commandToRun': '''docker tag cicdtest:2.0 us-east1-docker.pkg.dev/lftest-407308/cicdtest/cicdtest:2.0

/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud artifacts repositories describe cicdtest --project=lftest-407308 --location=us-east1

/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud auth print-access-token --impersonate-service-account 902295206386-compute@developer.gserviceaccount.com | docker login -u oauth2accesstoken --password-stdin https://us-east1-docker.pkg.dev

docker push us-east1-docker.pkg.dev/lftest-407308/cicdtest/cicdtest:2.0''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Enable services', {
      actualParameter = [
        'commandToRun': '/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud services enable compute.googleapis.com container.googleapis.com',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Create cluster', {
      actualParameter = [
        'commandToRun': '/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters create cicdtest-cluster  --num-nodes 2  --machine-type n1-standard-1  --zone us-east1-b',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Deploy app', {
      actualParameter = [
        'commandToRun': '''/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud container clusters get-credentials cicdtest-cluster --zone us-east1-b --project lftest-407308
/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl create deployment cicdtest --image=us-east1-docker.pkg.dev/lftest-407308/cicdtest/cicdtest:1.0
#cd /home/logicfocus/jai/cicdtest
#/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f db-deployment.yaml
#/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl apply -f app-Deployment.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Expose app', {
      actualParameter = [
        'commandToRun': '/home/logicfocus/gcloud/google-cloud-sdk/bin/kubectl create service loadbalancer cicdtest --tcp=8080:8080',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '5'
  }

  property 'CICDTestServiceId', value: '42296142', {
    suppressValueTracking = '1'
  }

  property 'gitLabAccessToken', value: 'glpat-_8xMsbHDzintdaxYMk-k', {
    suppressValueTracking = '1'
  }

  property 'referenceBranch', value: 'develop', {
    description = 'Reference branch from which a release branch will be created'
    suppressValueTracking = '1'
  }
}
