## Create Docker Network
```
docker network create -d bridge --subnet 192.168.2.0/24 --gateway 192.168.2.1 oipa
```

## Run Eclipse
```
cd ~/src/v10
(cd eclipse && docker-compose -p $(basename $(cd .. && pwd)) up -d)
ssh -X developer@eclipse run
```

## Start MS SQL
```
cd ~/src/v10
(cd db.mssql && docker-compose -p $(basename $(cd .. && pwd)) up -d)
```

## Start Query Tool
```
cd ~/src/v10
(cd query && docker-compose -p $(basename $(cd .. && pwd)) up -d)
ssh -X root@query run
```

## TODO
# Script to setup Github credentials
# WebSphere Container (app.websphere)
# Database(s)
## SQL Server (db.mssql)
# GUI -------
# Eclipse
# Query
# Palette
# Shortcuts
## Configure
### War File (how do download? use a shared volume maybe)
### Db File (how do download? user a shared volume maybe)
## Develop

## Nice to have
# Auto login to RDP


## Workflows
# Start for developer
# Start for configuration
# Restore db backup
# Deploy war file
# Both db and war
# Develop extensions
# Develop debugger / Rules Companion
