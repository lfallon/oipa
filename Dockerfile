FROM websphere-liberty:kernel

RUN apt-get update && apt-get install -y curl

ENV JVM_ARGS="-Dwas.debug.mode=true -Dcom.ibm.websphere.ras.inject.at.transform=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7777"
EXPOSE 7777

RUN mkdir -p /logs
VOLUME /logs
ENV LOG_DIR=/logs

ENV DB_HOST=104.208.247.218
ENV DB_NAME=OIPA_GA
ENV DB_USER=sqlUser
ENV DB_PASSWORD=sqlUser1
ENV DB_PORT=1433

COPY server.xml /config/
RUN installUtility install --acceptLicense defaultServer

# Install OIPA
ARG OIPA_VERSION=10.2.0.30
# ENV OIPA_VERSION=10.2.0.30
RUN curl --fail -o /config/apps/PASJava.war -O http://repo.pennassurancesoftware.com/artifactory/public/com/adminserver/PASJava/${OIPA_VERSION}/PASJava-${OIPA_VERSION}.war

# Config
COPY shared/ /opt/ibm/wlp/usr/shared/

# Extensions
ENV DEBUGGER_VERSION=1.04.015
RUN mkdir -p /extensions
RUN curl --fail -o /extensions/debugger-v10-${DEBUGGER_VERSION}.jar -O http://repo.pennassurancesoftware.com/artifactory/public/com/pennassurancesoftware/debugger-v10/${DEBUGGER_VERSION}/debugger-v10-${DEBUGGER_VERSION}.jar
COPY extensions.xml /extensions/
VOLUME /extensions

CMD ["/opt/ibm/wlp/bin/server", "run", "defaultServer"]


# Test
# docker build -t local/oipa .
# docker stop oipa; docker rm oipa; docker run -d --name oipa -p 9080:9080 -p 7777:7777 local/oipa; docker logs -f oipa
# docker stop oipa; docker rm oipa; docker run -d --name oipa -p 9080:9080 -p 7777:7777 -v /tmp/logs:/logs -e DB_NAME=OIPA_POC local/oipa; docker logs -f oipa
# docker stop oipa; docker rm oipa; docker run -d --name oipa -p 9080:9080 -p 7777:7777 -v /home/vagrant/git/oipa-tools/debugger-v10/dist:/extensions -e DB_NAME=OIPA_Securian local/oipa; docker logs -f oipa

# Notes
# Directory Structure:
# https://www.ibm.com/support/knowledgecenter/en/SS7K4U_liberty/com.ibm.websphere.wlp.zseries.doc/ae/rwlp_dirs.html

# Docker Repo:
# https://hub.docker.com/_/websphere-liberty/

# Tags
# pennassurancesoftware/oipa:10.2.0.30-sqlserver
# pennassurancesoftware/oipa:10.2.0.30-db2
# pennassurancesoftware/oipa:10.2.0.30-oracle
