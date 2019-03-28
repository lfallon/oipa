#!/bin/bash

echo "machine github.com login $EMAIL password $PASS" > ~/.netrc
git config --global user.email "${EMAIL}"
git config --global user.name "${NAME}"

CMD="git $@"

eval $CMD
