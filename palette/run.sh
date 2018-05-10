#!/bin/sh

function transform() {
  while read -r line ; do
    line=${line//\"/\\\"}
    line=${line//\`/\\\`}
    line=${line//\$/\\\$}
    line=${line//\\\${/\${}
    eval "echo \"$line\"";
  done < ${1}
}

# TODO
# Call Palette Web Service to get properties
# Delcare properties
# Transform properties file

declare RESOURCE_USER=install
declare RESOURCE_PASSWORD=install

function prop() {
  oipa-bootstrap palette $PALETTE_URL -u ${RESOURCE_USER} -p ${RESOURCE_PASSWORD} --prop $1
}

declare DB_HOST=$(prop PasDatabaseServer)
declare DB_NAME=$(prop PasDatabaseName)
declare PROP_JDBC_URL=$(oipa-bootstrap escape "jdbc:jtds:sqlserver://${DB_HOST}/${DB_NAME};prepareSQL=2")
declare PROP_JDBC_HOST=$(oipa-bootstrap escape "${DB_HOST}")
declare PROP_JDBC_USER=$(oipa-bootstrap escape "${DB_USER}")
declare PROP_JDBC_PASSWORD=$(oipa-bootstrap escape "$(oipa-bootstrap encrypt \"${DB_PASSWORD}\")")
declare PROP_JDBC_DB=$(oipa-bootstrap escape "${DB_NAME}")
declare PROP_RESOURCE_USER=$(oipa-bootstrap escape "${RESOURCE_USER}")
declare PROP_RESOURCE_PASSWORD=$(oipa-bootstrap escape "$(oipa-bootstrap encrypt \"${RESOURCE_PASSWORD}\")")
declare PROP_PALETTE_URL=$(oipa-bootstrap escape "${PALETTE_URL}")

declare TEMPLATE_HOME=/opt/jboss/RulesPalette/environment.templates/DOCKER

rm -Rf /opt/jboss/RulesPalette/PaletteEnvironments/RulesIDE/DOCKER*
if [ -z "$(prop IvsDatabaseType | xargs)" ] ; then
  declare ENV_HOME=/opt/jboss/RulesPalette/PaletteEnvironments/RulesIDE/DOCKER\(NO\ IVS\)
  mkdir -p "$ENV_HOME"
  transform ${TEMPLATE_HOME}/asenv.noivs.properties.template > "${ENV_HOME}/asenv.properties"
else
  declare ENV_HOME=/opt/jboss/RulesPalette/PaletteEnvironments/RulesIDE/DOCKER
  mkdir -p "$ENV_HOME"
  declare IVS_DB_HOST=$(prop IvsDatabaseServer)
  declare IVS_DB_NAME=$(prop IvsDatabaseName)
  declare PROP_JDBC_IVS_USER=${PROP_JDBC_USER}
  declare PROP_JDBC_IVS_PASSWORD=${PROP_JDBC_PASSWORD}
  declare PROP_JDBC_IVS_DB=$(oipa-bootstrap escape "${IVS_DB_NAME}")
  declare PROP_JDBC_IVS_URL=$(oipa-bootstrap escape "jdbc:jtds:sqlserver://${IVS_DB_HOST}/${IVS_DB_NAME};prepareSQL=2")
  transform ${TEMPLATE_HOME}/asenv.ivs.properties.template > "${ENV_HOME}/asenv.properties"
fi

# cat $ENV_HOME/asenv.properties

(cd /opt/jboss/RulesPalette && bin/asgraphicruleside)
