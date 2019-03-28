# Setup
## Install Docker
Install Docker => https://docs.docker.com/install/linux/docker-ce/ubuntu/

1. Uninstall old versions
    ```
    sudo apt-get remove docker docker-engine docker.io
    ```

2. Install Dependencies
    ```
    sudo apt-get update && \
    sudo apt-get install \
      apt-transport-https \
      ca-certificates \
      curl \
      software-properties-common
    ```
3. Install
    ```
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - && \
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" && \
    sudo apt-get update && \
    sudo apt-get install docker-ce
    ```
## Install Docker Compose
Install Docker Compose => https://docs.docker.com/compose/install/
```
sudo curl -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose && \
sudo chmod +x /usr/local/bin/docker-compose && \
docker-compose --version
```

## Permissions
1. Add current user to the `docker` group.
    ```
    sudo usermod -aG docker $USER
    ```
2. Restart computer.



## Clone Git Repository
```
mkdir -p ~/git && cd ~/git && git clone https://github.com/PennAssuranceSoftware/oipa.git && cd oipa
```

## Download Database Backups into the `env/restore` folder
```
mkdir -p env/restore
```
Copy your application database backup and IVS backup if you have one.

## Start

### NO IVS
Export the name of the database as the variable `DB_NAME` before starting.
```
export IVS=false && \
export DB_NAME=OIPA_SandBox && \
  docker-compose up
```

#### Developer Mode
```
export IVS=false && \
export DB_NAME=OIPA_SandBox && \
  docker-compose -f docker-compose.yml -f docker-compose.dev.yml rm -v -f && \
  docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

### IVS
Export the name of the database as the variables `DB_NAME`, `IVS`, and `IVS_DB_NAME` before starting.
```
export DB_NAME=OIPA_SandBox && \
export IVS=true && \
export IVS_DB_NAME=OIPA_IVS && \
  docker-compose up
```

#### Developer Mode
```
export DB_NAME=OIPA_SandBox && \
export IVS=true && \
export IVS_DB_NAME=OIPA_IVS && \
  docker-compose -f docker-compose.yml -f docker-compose.dev.yml rm -v -f && \
  docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

# Palette
Once you have setup your application you will be able to run the Palette in that environment. Navigate
to the `oipa` directory you cloned from git earlier and run the following command to start the Palette.
```
xhost + && docker-compose exec palette ./run.sh
```

# Query Tool (Squirrel SQL Client)
Once you have setup your application you will be able to query the database in that environment. Navigate
to the `oipa` directory you cloned from git earlier and run the following command to start the Squirrel
SQL Client.
```
xhost + && docker-compose exec query ./run.sh
```

# Java Decompiler
You can search and browse the OIPA war file and decompiled code with the decompiler. Once you have setup
your application you will be able to run the Java Decompiler in that environment. Navigate
to the `oipa` directory you cloned from git earlier and run the following command to start the Decompiler.
```
xhost + && docker-compose exec jd ./run.sh
```

# Eclipse
Once you have setup your application you will be able to run Eclipse in that environment. Navigate
to the `oipa` directory you cloned from git earlier and run the following command to start Eclipse.
```
xhost + && docker-compose exec eclipse ./run.sh
```

## Build Extensions

### Rebuild Extensions
```
docker-compose exec eclipse ./build.sh && docker-compose restart oipa
```


# Azure
You can also run using databases hosted on Azure if you don't have your own database backup.
Use the following command to start up the application pointing to an external database:
```
export IVS=false && \
export DB_HOST=104.208.247.218 && \
export DB_NAME=OIPA_Securian && \
export DB_USER=sqlUser && \
export DB_PASSWORD=sqlUser1 && \
  docker-compose up
```


# Docker Commands
## Clear Everything
```
docker stop $(docker ps -a -q)
docker ps -a | grep -i -E "Exit|Create" | grep -v ^CONTAINER | cut -d ' ' -f 1 | xargs docker rm
docker rmi -f $(docker images -q)
docker volume rm $(docker volume ls -f dangling=true -q)
```

# Mac Notes

## Temporary Workaround
The current compose does not work for Mac OSX. Make these changes until a permanent fix
can be made.

1. Comment out the `/dev/snd` lines in the `docker-compose.yml` file
2. Install `SOCAT`. http://macappstore.org/socat/
    ```
    brew install socat
    ```
3. Install `XQuartz`. https://www.xquartz.org/
    ```
    brew cask install xquartz
    ```
4. Open `XQuartz` and update the security settings
    ```
    open -a XQuartz
    ```
5. Run Socat
    ```
    socat TCP-LISTEN:6000,reuseaddr,fork UNIX-CLIENT:\"$DISPLAY\” &
    ```
6. You will need to override the `DISPLAY` environment variable when running any GUI app.
    ```
    export myIP=$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}') && \
    xhost + && docker-compose exec -e DISPLAY=$myIP:0 query ./run.sh
    ```

# Upload New War
1. Download `war` file to `env/upload` folder.
2. Upload to Maven Repository.
    ```
    make upgrade
    ```
3. Update the `.env` to the version of the `war` you just uploaded.
    ```
    export DOCKER_USER=jeromebridge && export DOCKER_PASS=XXX && make build push
    ```
4. Rebuild local Docker Compose Image.
    ```
    docker-compose build --no-cache oipa
    ```
# Backup database in Docker Container
1. To get-into-a-docker-containers-shell run this command
...
 docker exec -it oipa_db_1 bash
...
2. create link for sqlcmd in db container run this command
...
  ln -sfn /opt/mssql-tools/bin/sqlcmd /usr/bin/sqlcmd
...  
3.  connect to database with this command
...
sqlcmd -H localhost -U sa -P SQLServerPass1
...
4. backup database  This will perform a full backup of the database that you specify (“yourdbname”) to /var/opt/mssql/data
...
BACKUP DATABASE QA to DISK = 'QA.bak'
go
...
5. open new terminal window and go to git/oipa
6. In new terminal window Copy backup file from container to host by running this command.  It will copy the backup to you /Home directory.  You can change this target to where you want it copied to.
...
sudo docker cp oipa_db_1:/var/opt/mssql/data/QA.bak /Home/QA.bak
...
