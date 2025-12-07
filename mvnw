#!/bin/sh
export MAVEN_PROJECTBASEDIR=${MAVEN_PROJECTBASEDIR:-$(pwd)}
exec java -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -cp .mvn/wrapper/maven-wrapper.jar org.apache.maven.wrapper.MavenWrapperMain "$@"