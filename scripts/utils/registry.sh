#!/bin/bash
script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

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
    --app-image)
        P_APP_IMAGE=$2
        shift
        shift
        ;;
    --app-version)
        P_APP_VERSION=$2
        shift
        shift
        ;;
    --app-name)
        P_APP_IMAGE=$2
        shift
        shift
        ;;
  esac
done

if [[ "" == "${P_BRANCH}" ]] ; then
    P_BRANCH="master"
fi

. "${script_dir}/env/${P_BRANCH}.env"

if [[ "" == "${P_ACTION}" ]] ; then
    P_ACTION="login"
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

if [[ "login" == "${P_ACTION}" ]] ; then
    podman login ${P_IMG_REGISTRY_DOMAIN}
fi

if [[ "push" == "${P_ACTION}" ]] ; then
    podman tag "${P_APP_IMAGE}:${P_APP_VERSION}" "${P_IMG_REGISTRY_DOMAIN}/${P_APP_IMAGE}:${P_APP_VERSION}"
    podman push "${P_IMG_REGISTRY_DOMAIN}/${P_APP_IMAGE}:${P_APP_VERSION}"
fi