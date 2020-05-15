#!/bin/bash

echo date "WAR file will be named ${war}"
mkdir /tomcat/webapps
mv /app/work/ROOT.war /tomcat/webapps/${war}.war

exec java $JAVA_OPTIONS \
    -Dproperties=$PROPERTIES \
    -Dcatalina.home=/tomcat \
    -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager \
    -Djava.util.logging.config.file=/tomcat/conf/logging.properties \
    -classpath /tomcat/bin/bootstrap.jar:/tomcat/bin/tomcat-juli.jar:/tomcat/bin/tomcat-utils.jar \
    com.github.susom.tomcat.Bootstrap
