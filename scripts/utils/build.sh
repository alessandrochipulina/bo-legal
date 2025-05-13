#!/bin/bash
script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Función para mostrar ayuda y salir
show_help() {
  echo "Uso: $0 [opciones]"
  echo "Opciones:"
  echo "  --branch BRANCH        Rama a utilizar (por defecto: master)"
  echo "  --app-image APP_IMAGE  Nombre de la imagen"
  echo "  --app-version VERSION  Versión de la aplicación"
  exit 1
}

# Procesar parámetros con detección de errores
while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)
        if [[ -z "$2" || "$2" == --* ]]; then
          echo "Error: El parámetro --branch requiere un valor"
          show_help
        fi
        P_BRANCH=$2
        shift 2
        ;;
    --app-image)
        if [[ -z "$2" || "$2" == --* ]]; then
          echo "Error: El parámetro --app-image requiere un valor"
          show_help
        fi
        P_APP_IMAGE=$2
        shift 2
        ;;
    --app-version)
        if [[ -z "$2" || "$2" == --* ]]; then
          echo "Error: El parámetro --app-version requiere un valor"
          show_help
        fi
        P_APP_VERSION=$2
        shift 2
        ;;
    --help|-h)
        show_help
        ;;
    *)
        echo "Error: Parámetro desconocido: $1"
        show_help
        ;;
  esac
done

# Valores por defecto
if [[ "" == "${P_BRANCH}" ]]; then
    P_BRANCH="master"
fi

# Comprobar si el archivo de entorno existe
ENV_FILE="${script_dir}/env/${P_BRANCH}.env"
if [[ ! -f "$ENV_FILE" ]]; then
    echo "Error: El archivo de entorno para la rama '${P_BRANCH}' no existe: $ENV_FILE"
    exit 1
fi

# Cargar variables de entorno
. "$ENV_FILE"

if [[ "" == "${P_ACTION}" ]]; then
    P_ACTION="build"
fi

if [[ "" == "${P_APP_IMAGE}" ]]; then
    P_APP_IMAGE="${ENV_APP_IMAGE}"
fi

if [[ "" == "${P_APP_VERSION}" ]]; then
    P_APP_VERSION="${ENV_APP_VERSION}"
fi

echo "Configuración:"
echo "  Rama: ${P_BRANCH}"
echo "  Acción: ${P_ACTION}"
echo "  Imagen: ${P_APP_IMAGE}"
echo "  Versión: ${P_APP_VERSION}"

if [[ "build" == "${P_ACTION}" ]]; then
    echo "Iniciando proceso de build..."
    cd "${script_dir}/../.."
    podman build -f Dockerfile -t "${P_APP_IMAGE}:${P_APP_VERSION}" .
    BUILD_STATUS=$?
    if [[ $BUILD_STATUS -eq 0 ]]; then
        echo "Build completado exitosamente"
    else
        echo "Error en el proceso de build (código: $BUILD_STATUS)"
        exit $BUILD_STATUS
    fi
fi