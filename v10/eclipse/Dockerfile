FROM jeromebridge/eclipse-oxygen-docker

USER root

# Install Dependencies
RUN apt-get update && apt-get install lib32ncurses5 lib32z1 -y
RUN apt-get update && apt-get install libwebkitgtk-3.0-0 -y
RUN apt-get update && apt-get install dbus-x11 -y
RUN apt-get update && apt-get install nano
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

# Eclipse Plugins
RUN sudo chown -R $USER_NAME:$USER_NAME /home/developer/.eclipse
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.cfr.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.jad.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.jd.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.procyon.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,https://ecd-plugin.github.io/update/ -installIU org.sf.feeling.decompiler.source.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,http://download.jboss.org/jbosstools/updates/m2e-extensions/m2e-apt/ -installIU org.jboss.tools.maven.apt.feature.feature.group
RUN /opt/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/oxygen/,http://repo1.maven.org/maven2/.m2e/connectors/m2eclipse-tycho/0.7.0/N/0.7.0.201309291400/ -installIU org.sonatype.tycho.m2e.feature.feature.group

# Eclipse Settings
RUN sed -i -e 's/Xmx1024m/Xmx10536m/g' /opt/eclipse/eclipse.ini

# Eclipse: Disable JPA Feature
RUN ( cd /opt/eclipse && \
  mkdir -p disabled/features && \
  mkdir -p disabled/plugins && \
  mv plugins/org.eclipse.jpt.* disabled/plugins && \
  mv features/org.eclipse.jpt.* disabled/features )

# Scripts
RUN mkdir -p /tmp/scripts
COPY scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Environment Variables
ENV OIPA_TOOLS_SOURCE=${HOME}/git/oipa-tools
ENV DEBUGGER_SOURCE=${OIPA_TOOLS_SOURCE}/debugger-v10

# Volumes
USER ${USER_NAME}
RUN mkdir -p ${HOME}/.m2
RUN mkdir -p ${HOME}/git
RUN mkdir -p ${HOME}/scratch
RUN mkdir -p ${HOME}/ssh
VOLUME ${HOME}/.m2
VOLUME ${HOME}/git
VOLUME ${HOME}/scratch
USER root

ENTRYPOINT ["start"]
