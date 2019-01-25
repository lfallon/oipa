FROM dyokomizo/squirrel

RUN apt-get update && apt-get install maven rpl -y
RUN apt-get update && apt-get install -y openssh-server xauth

# Configure SSH
RUN mkdir /var/run/sshd \
    && sed -i "s/^.*PasswordAuthentication.*$/PasswordAuthentication no/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11Forwarding.*$/X11Forwarding yes/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11UseLocalhost.*$/X11UseLocalhost no/" /etc/ssh/sshd_config \
    && grep "^X11UseLocalhost" /etc/ssh/sshd_config || echo "X11UseLocalhost no" >> /etc/ssh/sshd_config \
    && grep "^PermitUserEnvironment" /etc/ssh/sshd_config || echo "PermitUserEnvironment yes" >> /etc/ssh/sshd_config

RUN mkdir /root/.ssh \
  && chmod 700 /root/.ssh \
  && ssh-keygen -A

ARG SQUIRREL_HOME=/opt/squirrel-sql-3.7

# PostgreSQL
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy \
  -DrepoUrl=https://repo.maven.apache.org/maven2/ \
  -Dartifact=org.postgresql:postgresql:9.4-1202-jdbc4:jar \
  -Dtransitive=false \
  -DoutputDirectory=$SQUIRREL_HOME/lib

# H2
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy \
  -DrepoUrl=https://repo.maven.apache.org/maven2/ \
  -Dartifact=com.h2database:h2:1.4.186:jar \
  -Dtransitive=false \
  -DoutputDirectory=$SQUIRREL_HOME/lib

# SQL Server
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy \
  -DrepoUrl=https://repo.maven.apache.org/maven2/ \
  -Dartifact=net.sourceforge.jtds:jtds:1.3.1:jar \
  -Dtransitive=false \
  -DoutputDirectory=$SQUIRREL_HOME/lib

# Scripts
RUN mkdir -p /tmp/scripts
COPY scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Config
RUN mkdir -p /root/.squirrel-sql
COPY conf/ /root/.squirrel-sql/
RUN sed -i 's/Xmx256m/Xmx1024m/g' /opt/squirrel-sql-3.7/squirrel-sql.sh

ENTRYPOINT ["start"]
