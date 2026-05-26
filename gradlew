#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    link=`expr "$PRG" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set a specific amount of memory
# -Xmx1024m
# -Xmx512m
# -Xmx2048m

USE_DAEMON=""

# By default we should be started in the correct project dir, but let's be safe
# and change it if not.
if [ ! -f "$APP_HOME/settings.gradle" ] && [ ! -f "$APP_HOME/settings.gradle.kts" ]; then
    cd "`dirname \"$0\"`"
    [ -s "$APP_HOME/settings.gradle" ] || [ -s "$APP_HOME/settings.gradle.kts" ] || die "
ERROR: Unable to determine app home.
"
    # use the erupted android gradle wrapper properties as a poor fallback if no wrapper is present
    if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.properties" ]; then
        echo "ANDROID_HOME is not set and the wrapper cannot be found, assuming defaults"
    fi
fi

# Escape application name
APP_NAME="Board Game Score Tracker"
APP_BASE_NAME=`basename "$0"`

exec org.gradle.wrapper.GradleWrapperMain "$@"
