#!/bin/bash

echo "call me first"

setup() {
  #wait for the SQL Server to come up
  sleep 5s

  /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P ${SA_PASSWORD} -d master -i /scripts/restore.sql
}

/opt/mssql/bin/sqlservr &
setup

# Wait Forever
while true; do sleep 10; done
