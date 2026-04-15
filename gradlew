#!/usr/bin/env sh

# Gradle wrapper script (POSIX).
# Minimal, compatible with Gradle 9.x.

set -e

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)

GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "ERROR: gradle wrapper jar manquant: $GRADLE_WRAPPER_JAR" >&2
  exit 1
fi

JAVA_EXEC="${JAVA_HOME:+$JAVA_HOME/bin/}java"

exec "$JAVA_EXEC" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$GRADLE_WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain \
  --gradle-user-home "$APP_HOME/.gradle" \
  --no-daemon \
  "$@"

