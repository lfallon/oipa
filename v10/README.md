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
cd ~/src/v10
(cd eclipse && docker-compose -p $(basename $(cd .. && pwd)) up -d)
ssh -X developer@eclipse run
```

## Start MS SQL
```
cd ~/src/v10
(cd db.mssql && docker-compose -p $(basename $(cd .. && pwd)) up -d --build)
```

## Restore Database Backup
1. Download the database backup in the `scratch` folder.
2. Run the restore command on the database container:
    ```
    cd ~/src/v10
    (cd db.mssql && docker-compose -p $(basename $(cd .. && pwd)) exec db restore)
    ```
3. Pick the backup to restore if more than one is in the `scratch` folder

# Start WebSphere
```
cd ~/src/v10
(cd app.websphere && docker-compose -p $(basename $(cd .. && pwd)) up -d)
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
# GUI -------
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
