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

FROM jboss/base-jdk:7

ARG VERSION=20180426T152435Z-001

# Alien
USER root
RUN yum -y install epel-release && yum clean all
RUN rpm --import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
RUN rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm
RUN yum -y update && yum -y install alien
RUN yum -y update && yum -y install openssh-server openssh-clients
RUN yum -y update && yum -y install which
RUN yum -y update && yum -y install xauth
USER jboss

# Configure SSH
USER root
RUN mkdir /var/run/sshd \
    && sed -i "s/^.*PasswordAuthentication.*$/PasswordAuthentication no/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11Forwarding.*$/X11Forwarding yes/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11UseLocalhost.*$/X11UseLocalhost no/" /etc/ssh/sshd_config \
    && grep "^X11UseLocalhost" /etc/ssh/sshd_config || echo "X11UseLocalhost no" >> /etc/ssh/sshd_config \
    && grep "^PermitUserEnvironment" /etc/ssh/sshd_config || echo "PermitUserEnvironment yes" >> /etc/ssh/sshd_config

RUN mkdir /root/.ssh \
  && chmod 700 /root/.ssh \
  && ssh-keygen -A
USER jboss

ENV URL=http://repo.pennassurancesoftware.com/artifactory/public/com/adminserver/RulesPalette/${VERSION}/RulesPalette-${VERSION}.zip
RUN echo Download: $URL
RUN curl -sSL "$URL" -o RulesPalette.zip
RUN unzip RulesPalette.zip

WORKDIR RulesPalette

RUN rm -Rf ./PaletteEnvironments/RulesIDE/*
RUN mkdir -p ./PaletteEnvironments/RulesIDE/configuration
COPY palette/conf/environments/configuration/ /opt/jboss/RulesPalette/PaletteEnvironments/RulesIDE/configuration/
RUN mkdir -p ./environment.templates/DOCKER
COPY palette/conf/environment.templates/DOCKER/ /opt/jboss/RulesPalette/environment.templates/DOCKER/

RUN rm -Rf ./etc/*
COPY palette/conf/etc/ /opt/jboss/RulesPalette/etc/
RUN chmod 755 /opt/jboss/RulesPalette/bin/asgraphicruleside

# Bootstrap
RUN mkdir -p /opt/jboss/installs
COPY --from=BOOTSTRAP /installs/ /opt/jboss/installs/
USER root
RUN cd /opt/jboss/installs && dpkg -i *.deb
USER jboss

# Scripts
USER root
RUN mkdir -p /tmp/scripts
COPY palette/scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Environment
ENV USER_NAME=jboss
ENV PALETTE_URL=
ENV DB_HOST=
ENV DB_NAME=
ENV DB_USER=
ENV DB_PASSWORD=
ENV PALETTE_URL=http://oipa:9080/PaletteConfig
ENV IVS=
ENV IVS_DB_NAME=

# User
RUN chsh -s /bin/bash $USER_NAME

ENTRYPOINT [ "start" ]
