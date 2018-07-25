#!/bin/sh

# Permissions
sudo chown -R $USER_NAME:$USER_NAME ${HOME}/.eclipse
sudo chown -R $USER_NAME:$USER_NAME ${ECLIPSE_WORKSPACE}
sudo chown -R $USER_NAME:$USER_NAME ${HOME}/git
sudo chown -R $USER_NAME:$USER_NAME ${HOME}/.m2
sudo chown -R $USER_NAME:$USER_NAME ${HOME}/extensions
sudo chown -R $USER_NAME:$USER_NAME ${HOME}/scratch

# Run
(cd ${DEBUGGER_SOURCE} && \
  mvn clean install && \
  cp ${DEBUGGER_SOURCE}/dist/debugger-v10-*.jar ${HOME}/extensions && \
  cp ${DEBUGGER_SOURCE}/src/main/config/extensions.xml ${HOME}/extensions )
