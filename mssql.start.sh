#!/bin/bash

_wait() {
  # Wait for the SQL Server to come up
  sleep 5s
}

start() {
  # Start MSSQL
  /opt/mssql/bin/sqlservr &
  _wait
}

restore() {
  start

  export ACTION=execute
  /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P ${SA_PASSWORD} -d master -i /scripts/restore.sql

  # Wait Forever
  while true; do sleep 10; done
}

print() {
  start &>/dev/null

  export ACTION=names
  /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P ${SA_PASSWORD} -d master -s, -h-1 -W -i /scripts/restore.sql
}

help() {
  /opt/mssql-tools/bin/sqlcmd -?
}


# Execute Command (If Present)
if [ -z "$1" ] ; then CMD=print ; else CMD=$1 ; fi

case $CMD in
  print-restore-db ) print ;;
  help ) help ;;
  * ) restore ;;
esac
