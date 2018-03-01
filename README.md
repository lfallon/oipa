# Running With Local SQL Server
```shell
docker run -d --name oipa -p 9080:9080 \
  -e DB_HOST=localhost \
  -e DB_NAME=OIPA_GA \
  -e DB_USER=sa \
  -e DB_PASSWORD=sa \
  jeromebridge/oipa:10.2.0.28-sqlserver
```

# Build And Push

Update the `build.env` file to be the version of OIPA to build and push for. Run the following commands in the console:

```shell
export DOCKER_USER=jeromebridge;
export DOCKER_PASS=XXXX;
make build
make push
```

# Run From MSSQL Backup File
1. Copy your database backup into the `env/restore` folder
2. Run Docker Compose to start the app `docker-compose build && export DB_NAME=$(docker-compose run -T --rm db print-restore-db) && docker-compose up`

# Develop Debugger
1. Copy your database backup into the `env/restore` folder
2. Compile the debugger v10 jar file
3. Copy the `debugger-v10-*.jar` file to the `env/extensions` folder 
    ```
    mkdir -p env/extensions && \
    (cd ~/git/oipa-tools/debugger-v10 && mvn clean install) && \
    cp ~/git/oipa-tools/debugger-v10/dist/debugger-v10-*.jar env/extensions
    ```
4. Clean volumes (docker-compose bug?)
    ```
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml rm -v -f
    ```
5. Run Docker Compose to start the app
    ```
    export DB_NAME=$(docker-compose run -T --rm db print-restore-db) && \
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
    ```

# Develop Extensions
1. Copy your database backup into the `env/restore` folder
2. Compile the extensions jar file
3. Copy the `extensions-*.jar` file to the `env/extensions` folder
    ```
    mkdir -p env/extensions && \
    (cd ~/git/oipa-tools/extensions-example && mvn clean install) && \
    cp ~/git/oipa-tools/extensions-example/dist/extensions*.jar env/extensions && \
    cp ~/git/oipa-tools/extensions-example/src/test/resources/extensions.xml env/extensions
    
    ```
4. Run Docker Compose to start the app 
    ```
    export DB_NAME=$(docker-compose run -T --rm db print-restore-db) && \
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
    ```

## Remote Database
```shell
docker stop oipa ; docker rm oipa ; \
(cd /home/vagrant/git/oipa-tools/debugger-v10/ && mvn clean install) && \
docker run -d --name oipa -p 9080:9080 -p 7777:7777 \
  -e DB_NAME=OIPA_Securian_Sandbox \
  -v /home/vagrant/git/oipa-tools/debugger-v10/dist:/extensions \
  jeromebridge/oipa:10.2.0.28-sqlserver && \
docker logs -f oipa
```

# Profiling
`service:jmx:rest:localhost:9443/IBMJMXConnectorREST`: `pasUser` / `pasUser`

# Notes

1. Upload war and artifacts to repo
2. Update build.env
3. commit and tag code
3.   a. create github release
4. push code
5. build docker image(s)
6. push docker tags

## Upload CLI
* Get the version
* Upload Artifacts
* Preview Artifacts that will be uploaded?
