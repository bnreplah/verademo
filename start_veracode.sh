#!/usr/bin/env bash

#### Setup variables ####

# Stop the script as soon as the first command fails
set -euo pipefail

# Set WEBHOOK to webhook secret (without URL)
WEBHOOK=$1

# Set the Veracode API ID
API_ID=$(echo $2 | cut -d "-" -f 2)

# Set the Veracode API SECRET
API_SECRET=$(echo $3 | cut -d "-" -f 2)

# Set the API endpoint
API_ENDPOINT="api.veracode.com"
API_PATH="/dae/api/core-api/webhook"

 generate_hmac_header() { 
 VERACODE_AUTH_SCHEMA="VERACODE-HMAC-SHA-256"
 VERACODE_API_VERSION="vcode_request_version_1"
 signing_data=$1 
 nonce="$(openssl rand -hex 16)"
 timestamp=$(date +%s"000")

 nonce_key=$(echo "$nonce" | xxd -r -p | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$API_SECRET" | awk -F" " '{ print $2 }')
 time_key=$(echo -n "$timestamp" | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$nonce_key" | awk -F" " '{ print $2 }')
 sig_key=$(echo -n "$VERACODE_API_VERSION" | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$time_key" | awk -F" " '{ print $2 }')
 signature=$(echo -n "$signing_data" | openssl dgst -sha256 -mac HMAC -macopt hexkey:"$sig_key" | awk -F" " '{ print $2 }')

echo "$VERACODE_AUTH_SCHEMA id=$API_ID,ts=$timestamp,nonce=$nonce,sig=$signature"
 } 

#### Start Security Scan ####

# Start Scan and get scan ID

signing_data="id=$API_ID&host=$API_ENDPOINT&url=$API_PATH/$WEBHOOK&method=POST"

VERACODE_AUTH_HEADER=$(generate_hmac_header $signing_data)

curl -X POST -H "Authorization: $VERACODE_AUTH_HEADER" --data "" https://$API_ENDPOINT$API_PATH/$WEBHOOK
