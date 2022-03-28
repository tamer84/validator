#!/bin/bash

secretstring=$(aws secretsmanager get-secret-value --secret-id $1 | sed 's/[\]//g' | sed 's/\"{/{/g' | sed 's/}\"/}/g' | jq ".SecretString")
user=$(jq ".user" <<< $secretstring | sed 's/\"//g')
password=$(jq ".password" <<< $secretstring | sed 's/\"//g')

jq -n --arg user $user --arg password $password '{"user":$user, "password":$password}'
