#!/bin/bash
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)
        P_BRANCH=$2
        shift
        shift
        ;;
    --username)
        P_USERNAME=$2
        shift
        shift
        ;;
    --password)
        P_PASSWORD=$2
        shift
        shift
        ;;
    --namespace)
        P_NAMESPACE=$2
        shift
        shift
        ;;
  esac
done

if [[ "" == "${P_BRANCH}" ]] ; then
    P_BRANCH="master"
fi

. "${script_dir}/env/${P_BRANCH}.env"

if [[ "" == "${P_NAME_SECRET_REGISTRY}" ]] ; then
    P_NAME_SECRET_REGISTRY="${ENV_NAME_SECRET_REGISTRY}"
fi

if [[ "" == "${P_USERNAME}" ]]; then
    echo "Ingrese parametro --username"
    exit 0
fi

if [[ "" == "${P_PASSWORD}" ]]; then
    echo "Ingrese parametro --password"
    exit 0
fi

if [[ "" == "${P_K8S_NAMESPACE}" ]] ; then
    P_K8S_NAMESPACE="${ENV_NAMESPACE}"
fi

if [[ "" == "${P_IMG_REGISTRY_DOMAIN}" ]] ; then
    P_IMG_REGISTRY_DOMAIN="${ENV_IMG_REGISTRY_DOMAIN}"
fi

kubectl create secret docker-registry ${P_NAME_SECRET_REGISTRY} --namespace ${P_K8S_NAMESPACE} --docker-server="${P_IMG_REGISTRY_DOMAIN}" --docker-username="${P_USERNAME}" --docker-password="${P_PASSWORD}"
