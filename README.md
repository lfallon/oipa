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
export IVS=false && export DB_NAME=OIPA_SandBox && docker-compose up
```

### IVS
Export the name of the database as the variables `DB_NAME`, `IVS`, and `IVS_DB_NAME` before starting.
```
export DB_NAME=OIPA_SandBox && export IVS=true && IVS_DB_NAME=OIPA_IVS && docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
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
xhost + && docker-compose exec eclipse /home/developer/scripts/run.sh
```

## Build Extensions

### Start In Developer Mode
```
export DB_NAME=OIPA_SandBox && docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

### Rebuild Extensions
```
docker-compose exec eclipse /home/developer/scripts/build.sh && \
docker-compose restart oipa
```
