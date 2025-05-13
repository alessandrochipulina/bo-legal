#!/bin/bash
script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. ${script_dir}/methods.sh

while [[ $# -gt 0 ]]; do
  case "$1" in
    --action)
        P_ACTION=$2
        shift
        shift
        ;;
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
    --k8s-app)
        P_K8S_APP=$2
        shift
        shift
        ;;
    --k8s-image)
        P_APP_IMAGE=$2
        shift
        shift
        ;;
    --k8s-version)
        P_APP_VERSION=$2
        shift
        shift
        ;;
    --k8s-deployment)
        P_K8S_DEPLOYMENT=$2
        shift
        shift
        ;;
    --k8s-service)
        P_K8S_SERVICE=$2
        shift
        shift
        ;;
    --k8s-namespace)
        P_NAMESPACE=$2
        shift
        shift
        ;;
    --k8s-host-port)
        P_K8S_HOST_PORT=$2
        shift
        shift
        ;;
    --k8s-svc-port)
        P_K8S_SVC_PORT=$2
        shift
        shift
        ;;
  esac
done

if [[ "" == "${P_BRANCH}" ]] ; then
    P_BRANCH="master"
fi

. ${script_dir}/env/${P_BRANCH}.env

if [[ "" == "${P_ACTION}" ]] ; then
    P_ACTION="run"
fi

if [[ "" == "${P_K8S_DEPLOYMENT}" ]] ; then
    P_K8S_DEPLOYMENT="${ENV_K8S_DEPLOYMENT}"
fi

if [[ "" == "${P_K8S_SERVICE}" ]] ; then
    P_K8S_SERVICE="${ENV_K8S_SERVICE}"
fi

if [[ "" == "${P_K8S_CFG_MAP}" ]] ; then
    P_K8S_CFG_MAP="${ENV_K8S_CFG_MAP}"
fi

if [[ "" == "${P_K8S_SECRET}" ]] ; then
    P_K8S_SECRET="${ENV_K8S_SECRET}"
fi

if [[ "" == "${P_NAMESPACE}" ]] ; then
    P_NAMESPACE="${ENV_NAMESPACE}"
fi

if [[ "" == "${P_K8S_SECRET_REGISTRY}" ]] ; then
    P_K8S_SECRET_REGISTRY="${ENV_K8S_SECRET_REGISTRY}"
fi

if [[ "" == "${P_K8S_HOST_PORT}" ]] ; then
    P_K8S_HOST_PORT="${ENV_K8S_HOST_PORT}"
fi

if [[ "" == "${P_K8S_SVC_PORT}" ]] ; then
    P_K8S_SVC_PORT="${ENV_K8S_SVC_PORT}"
fi

if [[ "" == "${P_K8S_APP}" ]] ; then
    P_K8S_APP="${ENV_K8S_APP}"
fi

if [[ "" == "${P_APP_IMAGE}" ]] ; then
    P_APP_IMAGE="${ENV_APP_IMAGE}"
fi

if [[ "" == "${P_APP_VERSION}" ]] ; then
    P_APP_VERSION="${ENV_APP_VERSION}"
fi

if [[ "" == "${P_IMG_REGISTRY_DOMAIN}" ]] ; then
    P_IMG_REGISTRY_DOMAIN="${ENV_IMG_REGISTRY_DOMAIN}"
fi

#VOLUME_NAME="${P_K8S_APP}"
#VOLUME_DIR="${VOLUMES_PATH}/${P_K8S_APP}"

export P_NAMESPACE="${P_NAMESPACE}"
export P_APP_IMAGE="${P_APP_IMAGE}"
export P_APP_VERSION="${P_APP_VERSION}"
export P_K8S_APP="${P_K8S_APP}"
export P_K8S_SERVICE="${P_K8S_SERVICE}"
export P_K8S_SECRET="${P_K8S_SECRET}"
export P_K8S_CFG_MAP="${P_K8S_CFG_MAP}"
export P_IMG_REGISTRY_DOMAIN=${P_IMG_REGISTRY_DOMAIN}
export P_K8S_SECRET_REGISTRY=${P_K8S_SECRET_REGISTRY}
export P_DOMAIN=${ENV_DOMAIN}
#export VOLUME_NAME_DATA="${VOLUME_NAME}-config"
#export VOLUME_DIR_DATA="${VOLUME_DIR}/config"

# Función para procesar archivos YAML antes de aplicarlos con kubectl
# Reemplaza $! por $ para permitir el uso de variables de escape en templates
apply_kubectl_with_replacements() {
    local template_file=$1
    local action=$2

    # Crear un archivo temporal para los reemplazos
    local temp_file=$(mktemp)

    # Expandir variables de entorno y reemplazar $! por $
    envsubst < "${template_file}" | sed 's/\$!/\$/g' > ${temp_file}

    # Aplicar o eliminar según la acción especificada
    if [[ "${action}" == "apply" ]]; then
        kubectl apply -f ${temp_file}
    elif [[ "${action}" == "delete" ]]; then
        kubectl delete -f ${temp_file}
    fi

    # Eliminar el archivo temporal
    rm -f ${temp_file}
}

# Función para crear configmap desde un directorio o archivo
create_configmap_from_source() {
    local name=$1
    local source=$2

    if [[ -d "${source}" ]]; then
        kubectl create configmap ${name} --namespace=${P_NAMESPACE} --from-file=${source}
    elif [[ -f "${source}" ]]; then
        kubectl create configmap ${name} --namespace=${P_NAMESPACE} --from-env-file=${source}
    else
        echo "Error: Source '${source}' is neither a file nor directory."
        return 1
    fi
}

case "${P_ACTION}" in
    "run")
        echo "IMAGE: ${P_APP_IMAGE}:${P_APP_VERSION}"
        echo "P_K8S_SECRET: ${P_K8S_SECRET}"
        # ${script_dir}/ingress-sign.sh --branch ${P_BRANCH} --action create
        kubectl create secret generic "${P_K8S_SECRET}" --namespace=${P_NAMESPACE} --from-env-file=${script_dir}/../k8s/secret/${P_BRANCH}.env
        #apply_kubectl_with_replacements "${script_dir}/../k8s/volumes.yaml" "apply"
        apply_kubectl_with_replacements "${script_dir}/../k8s/deployment.yaml" "apply"
        ;;
    "remove")
        apply_kubectl_with_replacements "${script_dir}/../k8s/deployment.yaml" "delete"
        #apply_kubectl_with_replacements "${script_dir}/../k8s/volumes.yaml" "delete"
        kubectl delete secret --namespace=${P_NAMESPACE} "${P_K8S_SECRET}"
        # ${script_dir}/ingress-sign.sh --branch ${P_BRANCH} --action delete
        ;;
    "stop")
        kubectl scale deployment ${P_K8S_APP} --namespace ${P_NAMESPACE} --replicas=0
        ;;
    "start")
        kubectl scale deployment ${P_K8S_APP} --namespace ${P_NAMESPACE} --replicas=1
        ;;
    "restart")
        ${script_dir}/k8s.sh --branch ${P_BRANCH} --action stop
        ${script_dir}/k8s.sh --branch ${P_BRANCH} --action start
        ;;
    "rerun")
        ${script_dir}/k8s.sh --branch ${P_BRANCH} --action remove
        ${script_dir}/k8s.sh --branch ${P_BRANCH} --action run
        ;;
    "tail")
        POD_NAME=$(kubectl get pod -n ${P_NAMESPACE} | grep "${P_K8S_APP}.*Running" | awk '{print $1}')
        while [[ "" == ${POD_NAME} ]] ; do
            echo "Waiting for pod in state Running ..."
            sleep 10
            POD_NAME=$(kubectl get pod -n ${P_NAMESPACE} | grep "${P_K8S_APP}.*Running" | awk '{print $1}')
        done
        kubectl logs -f --namespace ${P_NAMESPACE} ${POD_NAME}
        ;;
    "events")
        POD_NAME=$(kubectl get pod -n ${P_NAMESPACE} | grep ${P_K8S_APP} | awk '{print $1}')
        kubectl get events --namespace ${P_NAMESPACE} | grep ${POD_NAME}
        ;;
    "expose")
        # Exponer servicio mediante NodePort
        if [[ -f "${script_dir}/../k8s/service.yaml" ]]; then
            export P_K8S_HOST_PORT="${P_K8S_HOST_PORT}"
            export P_K8S_SVC_PORT="${P_K8S_SVC_PORT}"
            apply_kubectl_with_replacements "${script_dir}/../k8s/service.yaml" "apply"
        else
            kubectl expose deployment ${P_K8S_APP} --namespace=${P_NAMESPACE} \
                --type=NodePort --port=${P_K8S_SVC_PORT:-8080} \
                --target-port=${P_K8S_HOST_PORT:-8080} --name=${P_K8S_SERVICE}
        fi
        ;;
    "cfg-map-create")
        # Crear configmap desde directorio o archivo
        if [[ -d "${script_dir}/../k8s/configmap" ]]; then
            create_configmap_from_source "${P_K8S_CFG_MAP}" "${script_dir}/../k8s/configmap"
        elif [[ -f "${script_dir}/../k8s/configmap/${P_BRANCH}.env" ]]; then
            create_configmap_from_source "${P_K8S_CFG_MAP}" "${script_dir}/../k8s/configmap/${P_BRANCH}.env"
        else
            echo "No se encontró directorio o archivo de configmap para crear."
        fi
        ;;
    "cfg-map-backup")
        # Hacer backup del configmap actual
        kubectl get configmap ${P_K8S_CFG_MAP} --namespace=${P_NAMESPACE} -o yaml > "${script_dir}/../k8s/configmap/${P_K8S_CFG_MAP}-backup.yaml"
        echo "Backup creado en: ${script_dir}/../k8s/configmap/${P_K8S_CFG_MAP}-backup.yaml"
        ;;
    "cfg-map-update")
        # Actualizar configmap existente
        kubectl delete configmap ${P_K8S_CFG_MAP} --namespace=${P_NAMESPACE}
        ${script_dir}/k8s.sh --branch ${P_BRANCH} --action cfg-map-create
        ;;
    *)
        echo "Acción desconocida: ${P_ACTION}"
        echo "Acciones disponibles: run, remove, stop, start, restart, rerun, tail, events"
        echo "                      expose, cfg-map-create, cfg-map-backup, cfg-map-update"
        exit 1
        ;;
esac

# Limpiar variables de entorno exportadas
export P_NAMESPACE=
export P_APP_IMAGE=
export P_APP_VERSION=
export P_K8S_APP=
export P_K8S_SERVICE=
export P_K8S_SECRET=
export P_K8S_CFG_MAP=
export P_IMG_REGISTRY_DOMAIN=
export P_K8S_SECRET_REGISTRY=
export VOLUME_NAME_DATA=
export VOLUME_DIR_DATA=