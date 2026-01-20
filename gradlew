#!/bin/bash
# Gradle wrapper script for UNIX
# This file is auto-generated. For full version, use Android Studio or gradle wrapper command.

echo "Downloading Gradle wrapper..."
GRADLE_VERSION="8.4"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Run gradle directly
exec gradle "$@"
