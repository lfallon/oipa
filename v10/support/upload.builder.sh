#! /bin/bash -eu

set -o pipefail

clean() {
  rm -Rf /target/*
}

deploy() {
  mvn deploy
}

copy() {
  clean

  VERSION=$(grep --max-count=1 '<version>' pom.xml | awk -F '>' '{ print $2 }' | awk -F '<' '{ print $1 }')
  cp dist/*.jar /target
  cp dist/*.deb /target
  cp dist/*.zip /target
  cp -Rf $MAVEN_CONFIG /target
}

export -f clean
export -f copy
export -f deploy

# exec "$@"
bash -c "$@"
