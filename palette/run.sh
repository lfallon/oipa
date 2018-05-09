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

function prop() {
  oipa-bootstrap palette $PALETTE_URL -u install -p install --prop $1
}

declare DB_HOST=$(prop PasDatabaseServer)
declare DB_NAME=$(prop PasDatabaseName)
declare PROP_JDBC_URL=$(oipa-bootstrap escape "jdbc:jtds:sqlserver://${DB_HOST}/${DB_NAME};prepareSQL=2")
declare PROP_JDBC_HOST=$(oipa-bootstrap escape "${DB_HOST}")
declare PROP_JDBC_USER=$(oipa-bootstrap escape "${DB_USER}")
declare PROP_JDBC_PASSWORD=$(oipa-bootstrap escape "$(oipa-bootstrap encrypt \"${DB_PASSWORD}\")")
declare PROP_DB=$(oipa-bootstrap escape "${DB_NAME}")


echo $PROP_JDBC_URL
echo $PROP_JDBC_USER
echo $PROP_JDBC_PASSWORD
echo $PROP_JDBC_HOST

# (cd /opt/jboss/RulesPalette && bin/asgraphicruleside)
