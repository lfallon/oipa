FROM openjdk:8-jdk AS BOOTSTRAP

# ----
# Install Maven
RUN apt-get install curl tar bash
ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Source And Target
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD support/maven-settings.xml $MAVEN_CONFIG/settings.xml

# Resolve Dependencies
COPY support/bootstrap/pom.xml /usr/src/app/
RUN mvn --fail-never -B dependency:resolve

# Build And Install
COPY support/bootstrap/ ./
RUN mvn clean install

# Target
RUN mkdir -p /installs
RUN cp dist/*deb /installs

# =============================================================================================================================================

FROM websphere-liberty:8.5.5.9-kernel
ARG OIPA_VERSION=
ARG PALETTE_VERSION=
ARG DEBUGGER_VERSION=

# Setup WebSphere
RUN apt-get update && apt-get install -y curl
COPY app.websphere/conf/server.xml /config/
RUN installUtility install --acceptLicense defaultServer

# Install OIPA
RUN curl --fail -o /config/apps/PASJava.war -O http://repo.pennassurancesoftware.com/artifactory/public/com/adminserver/PASJava/${OIPA_VERSION}/PASJava-${OIPA_VERSION}.war

# Install Palette
RUN curl --fail -o /config/apps/PaletteConfig.war -O http://repo.pennassurancesoftware.com/artifactory/public/com/adminserver/PaletteConfig/${PALETTE_VERSION}/PaletteConfig-${PALETTE_VERSION}.war
RUN mkdir -p /uploads
COPY app.websphere/conf/palette/uploads/ /uploads/

# Extensions
RUN mkdir -p /extensions
RUN curl --fail -o /extensions/debugger-v10-${DEBUGGER_VERSION}.jar -O http://repo.pennassurancesoftware.com/artifactory/public/com/pennassurancesoftware/debugger-v10/${DEBUGGER_VERSION}/debugger-v10-${DEBUGGER_VERSION}.jar
COPY app.websphere/conf/extensions.xml /extensions/
VOLUME /extensions

# Bootstrap
RUN mkdir -p /installs
COPY --from=BOOTSTRAP /installs/ /installs/
RUN cd /installs && dpkg -i *.deb

# Scripts
RUN mkdir -p /tmp/scripts
COPY app.websphere/scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Shared
COPY app.websphere/conf/shared/ /opt/ibm/wlp/usr/shared/

# Scratch
RUN mkdir -p /scratch/logs

# Environment Variables
ENV LOG_DIR=/scratch/logs
ENV JVM_ARGS="-Xms512m -Xmx1024m -Dwas.debug.mode=true -Dcom.ibm.websphere.ras.inject.at.transform=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7777"
ENV DB_HOST=104.208.247.218
ENV DB_NAME=OIPA_GA
ENV DB_USER=sqlUser
ENV DB_PASSWORD=sqlUser1
ENV DB_PORT=1433
ENV IVS_DB_NAME=OIPA_IVS


EXPOSE 7777

CMD ["bootstrap"]


# Notes
# Directory Structure:
# https://www.ibm.com/support/knowledgecenter/en/SS7K4U_liberty/com.ibm.websphere.wlp.zseries.doc/ae/rwlp_dirs.html

# Docker Repo:
# https://hub.docker.com/_/websphere-liberty/

# Tags
# pennassurancesoftware/oipa:10.2.0.30-sqlserver
# pennassurancesoftware/oipa:10.2.0.30-db2
# pennassurancesoftware/oipa:10.2.0.30-oracle
