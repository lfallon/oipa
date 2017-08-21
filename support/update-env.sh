#!/bin/bash

# $1 : Environment file to update
# $2 : Variable Name
# $3 : Variable Value
PATH=$1
NAME=$2
VALUE=$3
DEF=${NAME}=${VALUE}

beginswith() { case $2 in "$1"*) echo 0;; *) echo 1;; esac; }
checkresult() { if [ $? = 0 ]; then echo TRUE; else echo FALSE; fi; }
nl() { if [ "$1" == "" ]; then echo "$1"; else echo "$1\n"; fi; }

# Attempt Update
OUTPUT=""
UPDATED=false
while read p; do
  if [ $(beginswith $NAME $p) == 0 ] ; then
    UPDATED=true;
    OUTPUT=$(nl "${OUTPUT}")${DEF}
  else
    OUTPUT=$(nl "${OUTPUT}")${p}
  fi
done <$PATH

# Append
if ! $UPDATED ; then
    OUTPUT=$(nl "${OUTPUT}")${DEF}
fi

echo -e $OUTPUT > $PATH
