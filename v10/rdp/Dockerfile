FROM danielguerra/ubuntu-xrdp:18.04

RUN apt-get update && \
  apt-get remove docker docker-engine docker.io && \
  apt-get install apt-transport-https ca-certificates curl software-properties-common -y && \
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - && \
  apt-key fingerprint 0EBFCD88 && \
  add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" && \
  apt-get update && \
  apt-get install docker-ce -y

RUN apt-get update && apt-get install net-tools xorg openbox iputils-ping nmap -y
RUN apt-get update && apt-get install openssh-client gvfs-fuse gvfs-backends -y
RUN apt-get update && apt-get install gedit -y
RUN apt-get update && apt-get install file-roller -y
RUN apt-get update && apt-get install nano -y
RUN add-apt-repository -y ppa:tista/adapta && apt-get update && apt-get install adapta-gtk-theme -y
RUN apt-get update && apt-get install fonts-roboto fonts-noto -y
RUN add-apt-repository -y ppa:papirus/papirus && apt-get update && apt-get install papirus-icon-theme -y
RUN apt-get update && apt-get install tilix -y
RUN apt-get purge xscreensaver -y
RUN apt-get update && apt-get install xfce4-whiskermenu-plugin mugshot -y

RUN curl -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose && \
  chmod +x /usr/local/bin/docker-compose && \
  docker-compose --version

# Icons
COPY icons/scalable/ /usr/share/icons/hicolor/scalable/apps/

# Scripts
RUN mkdir -p /tmp/scripts
COPY scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Menu
COPY menu/ /usr/share/applications/

# Desktop
COPY config/xfce4/ /etc/xdg/xfce4/

# TODO
# Selected Theme

# Volumes
RUN mkdir -p /home/ubuntu/scratch && chown ubuntu /home/ubuntu/scratch
RUN mkdir -p /home/ubuntu/src && chown ubuntu /home/ubuntu/src
VOLUME /home/ubuntu/scratch
VOLUME /home/ubuntu/src

CMD [ "setup" ]
