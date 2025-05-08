#!/bin/bash

function evalExistsRemoteDirectory() {
    REMOTE_DIR=$1
    REMOTE_RESULT="$(minikube ssh "if [[ -d ${REMOTE_DIR} ]] ; then echo true; else echo false; fi" | sed 's/["\n\r]//g')"
    if [[ "false" == "${REMOTE_RESULT}" ]] ; then
        RETURN_VAL=0
    else
        RETURN_VAL=1
    fi
    return "${RETURN_VAL}";
}

function showMsgCreateRemoteDirectory() {
    REMOTE_DIR=$1
    echo "mkdir -p '$REMOTE_DIR'"
    echo "chown docker:docker -R '$REMOTE_DIR'"
}