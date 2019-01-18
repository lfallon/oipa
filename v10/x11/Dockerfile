FROM ubuntu
RUN apt update \
    && apt install -y firefox \
                      openssh-server \
                      xauth \
    && mkdir /var/run/sshd \
    && mkdir /root/.ssh \
    && chmod 700 /root/.ssh \
    && ssh-keygen -A \
    && sed -i "s/^.*PasswordAuthentication.*$/PasswordAuthentication no/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11Forwarding.*$/X11Forwarding yes/" /etc/ssh/sshd_config \
    && sed -i "s/^.*X11UseLocalhost.*$/X11UseLocalhost no/" /etc/ssh/sshd_config \
    && grep "^X11UseLocalhost" /etc/ssh/sshd_config || echo "X11UseLocalhost no" >> /etc/ssh/sshd_config

ENTRYPOINT ["sh", "-c", "/usr/sbin/sshd && tail -f /dev/null"]


# Gist
# https://gist.github.com/udkyo/c20935c7577c71d634f0090ef6fa8393


# 1. setup ssh credentials between containers

# Short cuts to start services / applications
# Scratch volume


# OIPA
#  Developer
