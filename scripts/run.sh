#!/bin/bash
#Implementación basada en https://tomd.xyz/camel-maven/

script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Cargar variables de entorno desde archivo .env en la raíz
if [[ -f "${script_dir}/.env" ]]; then
    . "${script_dir}/.env"
fi

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
    --app-name)
        P_APP_NAME=$2
        shift
        shift
        ;;
    --app-version)
        P_APP_VERSION=$2
        shift
        shift
        ;;
    --container-name)
        P_CONTAINER_NAME=$2
        shift
        shift
        ;;
    --k8s-namespace)
        P_K8S_NAMESPACE=$2
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

# Cargar variables específicas de la rama si se especificó una
if [[ "" != "${P_BRANCH}" ]] ; then
    if [[ -f "${script_dir}/utils/env/${P_BRANCH}.env" ]]; then
        . "${script_dir}/utils/env/${P_BRANCH}.env"
    fi
else
    P_BRANCH="master"
    if [[ -f "${script_dir}/utils/env/${P_BRANCH}.env" ]]; then
        . "${script_dir}/utils/env/${P_BRANCH}.env"
    fi
fi

# Valores predeterminados
if [[ "" == "${P_ACTION}" ]] ; then
    P_ACTION="run"
fi

if [[ "" == "${P_APP_NAME}" ]] ; then
    P_APP_NAME="${ENV_APP_NAME:-${ENV_APP_IMAGE}}"
fi

if [[ "" == "${P_APP_VERSION}" ]] ; then
    P_APP_VERSION="${ENV_APP_VERSION}"
fi

if [[ "" == "${P_CONTAINER_NAME}" ]] ; then
    P_CONTAINER_NAME="${P_APP_NAME:-${ENV_APP_CONTAINER}}"
fi

# Determinar el sistema de contenedores a utilizar (docker o podman)
CONTAINER_ENGINE="docker"
if ! command -v docker &> /dev/null && command -v podman &> /dev/null; then
    CONTAINER_ENGINE="podman"
fi

# Funciones para operaciones comunes
function stop_container() {
    local container_name=$1
    if [[ "" != "$(${CONTAINER_ENGINE} ps | grep ${container_name})" ]] ; then
        ${CONTAINER_ENGINE} stop "${container_name}"
    fi
}

# Ejecutar la acción solicitada
case "${P_ACTION}" in
    "add-genapiclient")
        ./mvnw quarkus:add-extension -Dextensions="io.quarkiverse.openapi.generator:quarkus-openapi-generator"
        ;;
    "build")
    echo "procesando build"
        if [[ "${CONTAINER_ENGINE}" == "docker" ]]; then
            ${script_dir}/utils/build.sh --app-image "${P_APP_NAME}" --app-version "${P_APP_VERSION}" --branch ${P_BRANCH}
        else
            ${script_dir}/utils/build.sh --app-image "${P_APP_NAME}" --app-version "${P_APP_VERSION}" --branch ${P_BRANCH}
        fi
        ;;
    "publish")
        ${script_dir}/utils/registry.sh --action login
        ${script_dir}/utils/registry.sh --app-name "${P_APP_NAME}" --app-version "${P_APP_VERSION}" --action push
        ;;
    "run")
        if [[ "${CONTAINER_ENGINE}" == "docker" ]]; then
            # Crear red virtual si no existe
            if [[ "" == "$(docker network list | grep ${ENV_VLAN:-bridge})" ]] ; then
                docker network create -d bridge "${ENV_VLAN:-bridge}"
            fi

            docker run -d --name "${P_CONTAINER_NAME}" \
                --env-file="${script_dir}/module/docker/.env" --network ${ENV_VLAN:-bridge} \
                --restart unless-stopped -p 8081:8080 \
                "${P_APP_NAME}:${P_APP_VERSION}"
        else
            podman run -d --name "${P_CONTAINER_NAME}" \
                --env-file="${script_dir}/module/podman/master.env" \
                --restart unless-stopped -p 8081:8080 \
                "${P_APP_NAME}:${P_APP_VERSION}"
        fi
        ;;
    "run:dev")
        ./mvnw quarkus:dev -DdebugHost=0.0.0.0
        ;;
    "stop")
        stop_container "${P_CONTAINER_NAME}"
        ;;
    "remove")
        stop_container "${P_CONTAINER_NAME}"
        ${CONTAINER_ENGINE} rm "${P_CONTAINER_NAME}"
        ;;
    "tail")
        ${CONTAINER_ENGINE} logs -f "${P_CONTAINER_NAME}"
        ;;
    "start")
        ${CONTAINER_ENGINE} start "${P_CONTAINER_NAME}"
        ;;
    "restart")
        stop_container "${P_CONTAINER_NAME}"
        ${CONTAINER_ENGINE} start "${P_CONTAINER_NAME}"
        ;;
    # Operaciones Kubernetes
    "k8s-run")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action run
        ;;
    "k8s-remove")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action remove
        ;;
    "k8s-restart")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action restart
        ;;
    "k8s-tail")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action tail
        ;;
    "k8s-events")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action events
        ;;
    "k8s-expose")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --k8s-host-port "${P_K8S_HOST_PORT}" --k8s-svc-port "${P_K8S_SVC_PORT}" --action expose
        ;;
    "k8s-cfg-map-create")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action cfg-map-create
        ;;
    "k8s-cfg-map-backup")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action cfg-map-backup
        ;;
    "k8s-cfg-map-update")
        ${script_dir}/utils/k8s.sh --branch ${P_BRANCH} --action cfg-map-update
        ;;
    # Operaciones de PostgreSQL
    "pg-backup")
        "${script_dir}/utils/postgres/maintain.sh" --branch ${P_BRANCH} --action backup --host ${ENV_DB_HOSTNAME}
        ;;
    "pg-restore")
        "${script_dir}/utils/postgres/maintain.sh" --branch ${P_BRANCH} --action restore --host ${ENV_DB_HOSTNAME} --database ${ENV_DB_NAME}
        ;;
    *)
        echo "Acción desconocida: ${P_ACTION}"
        echo "Acciones disponibles: add-genapiclient, build, publish, run, run:dev, stop, remove, tail, start, restart"
        echo "                      k8s-run, k8s-remove, k8s-restart, k8s-tail, k8s-events, k8s-expose"
        echo "                      k8s-cfg-map-create, k8s-cfg-map-backup, k8s-cfg-map-update"
        echo "                      pg-backup, pg-restore"
        exit 1
        ;;
esac

exit 0