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
docker ps -a | grep -i -E "Exit|Create" | grep -v ^CONTAINER | cut -d ' ' -f 1 | xargs sudo docker rm
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
