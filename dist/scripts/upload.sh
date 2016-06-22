#!/usr/bin/env bash

echo "Syncing local '$2' with remote '$4'"

rsync -u -rave "ssh -i $1" $2/* $3:$4