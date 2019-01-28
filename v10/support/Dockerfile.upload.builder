FROM openjdk:8-jdk

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
VOLUME /target

ADD maven-settings.xml $MAVEN_CONFIG/settings.xml

# Resolve Dependencies
COPY upload/pom.xml /usr/src/app/
RUN mvn --fail-never -B dependency:resolve

# Build And Install
COPY upload/ ./
RUN mvn clean install

ADD upload.builder.sh /
RUN chmod 755 /upload.builder.sh
ENTRYPOINT ["/upload.builder.sh"]
