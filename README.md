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
2. Run Docker Compose to start the app `export DB_NAME=OIPA_Sandbox && docker-compose up`


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
