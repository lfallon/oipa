## Create Docker Network
```
docker network create -d bridge --subnet 192.168.2.0/24 --gateway 192.168.2.1 oipa
```

## Run Eclipse
```
cd /src/v10
docker-compose -p $(basename $(cd .. && pwd)) up -d
ssh -X developer@eclipse run
```

## TODO
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
