#!/bin/bash
export script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. ${script_dir}/methods.sh

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)
        P_BRANCH=$2
        shift
        shift
        ;;
    -a|--action)
        ACTION=$2
        shift
        shift
        ;;
  esac
done

function evalEnvVars() {
     IS_STOP=false
     if [[ "" == "${ENV_DOMAIN}" ]]; then
          echo "No se ha definido la variable ENV_DOMAIN"; IS_STOP=true
     fi
     if [[ "" == "${ENV_APP_CONTAINER}" ]]; then
          echo "No se ha definido la variable ENV_APP_CONTAINER"; IS_STOP=true
     fi
     if [[ "true" == "${IS_STOP}" ]] ; then
       exit 1
     fi
 }

function evalCertsDir() {
  DIR_CERTS=$1
  DOMAIN=$2
  RESULT_LS=$(ls $DIR_CERTS | grep ${DOMAIN})
  if [[ "" == ${RESULT_LS} ]] ; then
      echo "No se encontro el directorio $DIR_CERTS/${DOMAIN}"
      exit 0
  fi
}

if [[ "" == "${P_BRANCH}" ]] ; then
    P_BRANCH="master"
fi

. ${script_dir}/env/${P_BRANCH}.env

evalEnvVars

INGRESS_DOMAIN="${ENV_DOMAIN}"
APP_CONTAINER="${ENV_APP_CONTAINER}"

evalCertsDir ${script_dir}/certs/archive ${INGRESS_DOMAIN}
evalCertsDir ${script_dir}/certs/live ${INGRESS_DOMAIN}

if [[ "" == "${NAMESPACE}" ]]; then
    NAMESPACE="${ENV_NAMESPACE}"
fi

if [[ "create" == $ACTION ]] ; then
    kubectl create secret tls "${APP_CONTAINER}-sign-tls" --namespace ${NAMESPACE} --key=${DIR_CERTS}/${DOMAIN}/privkey.pem --cert=${DIR_CERTS}/${DOMAIN}/fullchain.pem
fi

if [[ "delete" == $ACTION ]] ; then
    kubectl delete secret "${APP_CONTAINER}-sign-tls" --namespace ${NAMESPACE}
fi