#!/bin/bash

function transform() {
  while read -r line ; do
    line=${line//\"/\\\"}
    line=${line//\`/\\\`}
    line=${line//\$/\\\$}
    line=${line//\\\${/\${}
    eval "echo \"$line\"";
  done < ${1}
}

declare PALETTE_VERSION=${PALETTE_VERSION}
declare PALETTE_NBU=${PALETTE_NBU:=No}
declare DB_NAME=${DB_NAME}
declare IVS=${IVS:=false}
declare IVS_DB_NAME=${IVS_DB_NAME:=OIPA_IVS}
declare IVS_ENV=${IVS_ENV:=sandbox}
declare IVS_TRACK=${IVS_TRACK:=1}
declare IVS_RELEASE_MANAGEMENT=${IVS_RELEASE_MANAGEMENT:=on}
declare IVS_USE_PRODUCTS=${IVS_USE_PRODUCTS:=true}

if [ "$IVS" = true ] ; then
  transform /uploads/EnvironmentConfig.ivs.xml.template > /uploads/EnvironmentConfig.xml
else
  transform /uploads/EnvironmentConfig.noivs.xml.template > /uploads/EnvironmentConfig.xml
fi

# Start
/opt/ibm/wlp/bin/server run defaultServer
