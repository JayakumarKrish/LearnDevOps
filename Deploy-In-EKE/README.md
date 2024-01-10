//To clear kube config
kubectl config unset users
kubectl config unset contexts
kubectl config unset clusters
kubectl config unset current-context

//Configure AWS
aws configure

//Configure EKS with cluster
aws eks --region <region> update-kubeconfig --name <Clustername>

/CDRO/createCluster.yaml config file will help us to create cluster with nodes based on specified configuration in it to AWS.
