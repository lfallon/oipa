## Create Docker Network
```
docker network create -d bridge --subnet 192.168.2.0/24 --gateway 192.168.2.1 oipa
```

## Start
```
docker-compose up -d --build
```

## Stop
```
docker-compose down --remove-orphans
```

## Run Eclipse
```
cd ~/src/v10/eclipse
(docker-compose -p $(basename $(cd .. && pwd)) up -d --build)
ssh -X developer@eclipse run
```

## Start MS SQL
```
cd ~/src/v10/db.mssql
docker-compose -p $(basename $(cd .. && pwd)) up -d --build
```

## Restore Database Backup
1. Download the database backup in the `scratch` folder.
2. Run the restore command on the database container:
    ```
    cd ~/src/v10/db.mssql
    (docker-compose -p $(basename $(cd .. && pwd)) exec db restore interactive)
    ```
3. Pick the backup to restore if more than one is in the `scratch` folder

## Start WebSphere
### NO IVS
Export the name of the database as the variable `DB_NAME` before starting.
```
cd ~/src/v10/app.websphere
export IVS=false && export DB_NAME=OIPA_SandBox && docker-compose -p $(basename $(cd .. && pwd)) up -d --build
```

### IVS
Export the name of the database as the variables `DB_NAME`, `IVS`, and `IVS_DB_NAME` before starting.
```
cd ~/src/v10/app.websphere
(export DB_NAME=OIPA_SandBox && export IVS=true && export IVS_DB_NAME=OIPA_IVS && docker-compose -p $(basename $(cd .. && pwd)) up -d)
```

## Start Query Tool
```
cd ~/src/v10/query
docker-compose -p $(basename $(cd .. && pwd)) up -d
ssh -X root@query run
```

## Start Palette
### NO IVS
```
cd ~/src/v10/palette
export IVS=false && export DB_NAME=OIPA_SandBox && docker-compose -p $(basename $(cd .. && pwd)) up -d --build
ssh -X jboss@palette run
```

### IVS
```
cd ~/src/v10/palette
export export DB_NAME=OIPA_SandBox && export IVS=true && export IVS_DB_NAME=OIPA_IVS && docker-compose -p $(basename $(cd .. && pwd)) up -d --build
ssh -X jboss@palette run
```

## Build / Deploy Extensions
```
cd ~/src/v10/eclipse
docker-compose -p $(basename $(cd .. && pwd)) exec eclipse build
cd ~/src/v10/app.websphere
docker-compose -p $(basename $(cd .. && pwd)) restart oipa
```

## TODO
# Script to setup Github credentials
# WebSphere Container (app.websphere)
# Database(s)
# GUI -------
# Palette
# Shortcuts
## Configure
### War File (how do download? use a shared volume maybe)
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
