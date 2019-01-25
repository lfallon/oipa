FROM microsoft/mssql-server-linux:2017-GA

RUN apt-get update && apt-get install dos2unix

ENV ACCEPT_EULA=Y
ENV SA_PASSWORD=SQLServerPass1

# Scripts
RUN mkdir -p /tmp/scripts
COPY scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# SQL
RUN mkdir -p /sql
COPY sql/ /sql/


VOLUME /restore

ENTRYPOINT ["entry"]


# Build
# docker build -t local/mssql -f Dockerfile.db.mssql .

# Run
# docker stop mssql; docker rm mssql; docker volume rm mssql_data
# docker volume create --name mssql_data
# docker run -d --name mssql -v mssql_data:/var/opt/mssql -v $(pwd)/env/restore:/restore -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=SQLServerPass1' -p 1433:1433 local/mssql

# Print Restore Db Name
# docker run -it --rm -v $(pwd)/env/restore:/restore local/mssql print-restore-db




# NOTES
# sqlcmd -S %1 -i select.sql -v dbname = %2 tablename = %3 -o %4
#
#
# docker exec -it mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P SQLServerPass1
#   > EXEC master..xp_dirtree "/restore", 10, 1
#   > RESTORE FILELISTONLY FROM DISK = '/restore/OIPA_Sandbox_backup_20170821070015.bak'
#   > RESTORE DATABASE OIPA FROM DISK = '/restore/OIPA_Sandbox_backup_20170821070015.bak' WITH REPLACE;
#   > SELECT NAME FROM SYS.DATABASES
