pipeline 'Deploy-In-EKS', {
  description = 'AWS'
  projectName = 'jai'

  formalParameter 'releaseVersion', defaultValue: '', {
    label = 'Release version'
    orderIndex = '1'
    type = 'entry'
  }

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  stage 'Build', {
    colorCode = '#289ce1'
    pipelineName = 'Deploy-In-EKS'
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

    task 'CI Build', {
      actualParameter = [
        'releaseVersion': '$[releaseVersion]',
      ]
      ciConfigurationName = 'jenkins75'
      ciJobFolder = 'Jai/'
      ciJobName = 'EKE'
      subproject = 'jai'
      taskType = 'CI_JOB'
    }
  }

  stage 'Deploy app in EKE', {
    colorCode = '#ff7f0e'
    pipelineName = 'Deploy-In-EKS'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task 'Get Deployment and Service File', {
      actualParameter = [
        'artifacts': '*.yaml',
        'build_number': '',
        'config_name': '/projects/jai/pluginConfigurations/JenkinsCICDTestService',
        'job_name': 'EKE',
        'target_directory': '',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Jenkins'
      subprocedure = 'DownloadArtifacts'
      taskType = 'PLUGIN'
    }

    task 'Create cluster', {
      actualParameter = [
        'commandToRun': 'eksctl create cluster --name lf-test --version 1.23 --region us-east-1 --nodegroup-name lf-test-nodes --node-type t2.micro --nodes 2',
      ]
      enabled = '0'
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Create nodes', {
      actualParameter = [
        'commandToRun': '''#eksctl create nodegroup  --cluster lf-test  --region us-east-1 --name lfnodegrp --node-type t2.micro --nodes 2 --node-security-groups sg-0543f6153e6159486
#eksctl create nodegroup -f /home/logicfocus/jai/createNode.yaml --dry-run
eksctl create cluster -f /home/logicfocus/jai/createNode.yaml''',
      ]
      enabled = '0'
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Deploy', {
      actualParameter = [
        'commandToRun': '''cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
kubectl apply -f k0s-deployment.yaml --request-timeout=4s ''',
      ]
      enabled = '0'
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Expose', {
      actualParameter = [
        'commandToRun': '''cd /opt/cloudbees/sda/workspace/$[/myStageRuntime/tasks/Get Deployment and Service File/job/name]
kubectl apply -f k0s-deployment.yaml''',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Check', {
      actualParameter = [
        'commandToRun': '''kubectl get deployments
echo "================"
kubectl get pods
echo "================"
kubectl get service
echo "================"
kubectl get nodes -o wide''',
      ]
      enabled = '0'
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '60'
  }

  property 'CICDTestServiceId', value: '42296142', {
    suppressValueTracking = '1'
  }

  property 'gitLabAccessToken', value: 'glpat-_8xMsbHDzintdaxYMk-k', {
    suppressValueTracking = '1'
  }

  property 'referenceBranch', value: 'develop', {
    suppressValueTracking = '1'
  }
}
