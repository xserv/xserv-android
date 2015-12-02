#!/bin/bash

./gradlew clean build bintrayUpload -p xserv-android
# ./gradlew clean build bintrayUpload