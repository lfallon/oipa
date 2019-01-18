FROM openkbs/eclipse-oxygen-docker

USER root

RUN apt-get update && apt-get install lib32ncurses5 lib32z1 -y

# Scripts
RUN mkdir -p /tmp/scripts
COPY scripts/ /tmp/scripts/
RUN chmod +x /tmp/scripts/* && mv /tmp/scripts/* /usr/local/bin/

# Volumes
RUN mkdir -p ${HOME}/.m2
RUN mkdir -p ${HOME}/git
RUN mkdir -p ${HOME}/scratch
VOLUME ${HOME}/.m2
VOLUME ${HOME}/git
VOLUME ${HOME}/scratch

# User
USER $USER_NAME

CMD ["run"]


# docker build -t local/eclipse -f Dockerfile.eclipse .
# docker run -it --rm -v $(pwd)/env/eclipse/workspace:/home/developer/eclipse-workspace -v $(pwd)/env/eclipse/prefs:/home/developer/.eclipse -v $(pwd)/env/eclipse/m2:/home/developer/.m2 -v $(pwd)/env/eclipse/git:/home/developer/git -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=unix$DISPLAY --device /dev/snd --name eclipse local/eclipse


# mkdir -p /tmp/ws && chmod 777 /tmp/ws && mkdir -p /tmp/.eclipse && chmod 777 /tmp/.eclipse && docker run -it --rm -v /tmp/ws:/home/developer/eclipse-workspace -v /tmp/.eclipse:/home/developer/.eclipse -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=unix$DISPLAY --device /dev/snd --name eclipse openkbs/eclipse-oxygen-docker
