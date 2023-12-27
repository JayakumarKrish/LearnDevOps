pipeline 'Deploy-Using-Terraform', {
  projectName = 'jai'

  formalParameter 'releaseVersion', defaultValue: '', {
    label = 'Release Version'
    orderIndex = '1'
    type = 'entry'
  }

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  stage 'Build', {
    colorCode = '#289ce1'
    pipelineName = 'Deploy-Using-Terraform'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task 'Create Branch', {
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
      ciJobName = 'CICDTestService'
      subproject = 'jai'
      taskType = 'CI_JOB'
    }
  }

  stage 'Deploy via Terraform', {
    colorCode = '#ff7f0e'
    pipelineName = 'Deploy-Using-Terraform'
    gate 'PRE', {
      }

    gate 'POST', {
      }

    task ' Retrieve artifact', {
      actualParameter = [
        'artifactId': 'cicdtestservice',
        'classifier': '',
        'config': '/projects/Logicfocus/pluginConfigurations/Nexus-config',
        'destination': '/home/logicfocus/terraform/cicdTestApp',
        'extension': 'zip',
        'groupId': 'com.logicfocus',
        'latestVersion': '0',
        'overwrite': '1',
        'repository': 'CICDTestService',
        'repoType': 'maven',
        'resultPropertySheet': '',
        'version': '$[releaseVersion]',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Nexus'
      subprocedure = 'Retrieve Artifact from Nexus'
      taskType = 'PLUGIN'
    }

    task 'Extract', {
      actualParameter = [
        'commandToRun': 'unzip /home/logicfocus/terraform/cicdTestApp/cicdtestservice-$[releaseVersion].zip -d /home/logicfocus/terraform/cicdTestApp',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Core'
      subprocedure = 'RunCommand'
      taskType = 'COMMAND'
    }

    task 'Init working Dir', {
      actualParameter = [
        'config': '/projects/jai/pluginConfigurations/TerraForm75Server',
        'tfCommandArguments': '',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Terraform'
      subprocedure = 'Init'
      taskType = 'PLUGIN'
    }

    task 'Plan', {
      actualParameter = [
        'config': '/projects/jai/pluginConfigurations/TerraForm75Server',
        'tfCommandArguments': '',
        'tfPlanName': 'plan',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Terraform'
      subprocedure = 'Plan'
      taskType = 'PLUGIN'
    }

    task 'Create VM', {
      actualParameter = [
        'config': '/projects/jai/pluginConfigurations/TerraForm75Server',
        'tfCommandArguments': '',
        'tfOutputProperty': '/myJob/showResults',
        'tfPlanName': 'plan',
      ]
      resourceName = 'LFServer75'
      subpluginKey = 'EC-Terraform'
      subprocedure = 'Apply'
      taskType = 'PLUGIN'
    }

    task 'Deploy', {
      actualParameter = [
        'commandToRun': '/home/logicfocus/gcloud/google-cloud-sdk/bin/gcloud compute ssh logicfocus_devops@lf-instancetest --command="nohup java -jar /tmp/gradleSampleArt.jar  > /tmp/output.log 2>&1 &" --zone us-east1-b',
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
    pipelineCounter = '562'
  }

  property 'ec_customEditorData', {

    // Custom properties

    property 'parameters', {
      propertyType = 'sheet'
    }
  }

  property 'CICDTestServiceId', value: '42296142', {
    suppressValueTracking = '1'
  }

  property 'GCPProjectId', value: 'lftest-407308', {
    suppressValueTracking = '1'
  }

  property 'artifactBuildPath', value: '/home/logicfocus/jai/Build.txt', {
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
