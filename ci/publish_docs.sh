#! /bin/bash

set -e

source ./ci/common.sh

info "Publishing docs for: $RELEASE_VERSION"
git config --global user.email "evangelists@stormpath.com"
git config --global user.name "stormpath-sdk-java Auto Doc Build"
git clone git@github.com:stormpath/stormpath.github.io.git
cd stormpath.github.io
git fetch origin source:source
git checkout source

info "Copying over servlet plugin docs"
rm -rf source/java/servlet-plugin/
cp -r ../docs/build/html/servlet source/java/servlet-plugin
cp -r ../docs/build/html/servlet source/java/servlet-plugin/latest
cp -r ../docs/build/html/servlet source/java/servlet-plugin/$RELEASE_VERSION

info "Copying over spring boot docs"
rm -rf source/java/spring-boot-web
cp -r ../docs/build/html/springboot source/java/spring-boot-web
cp -r ../docs/build/html/springboot source/java/spring-boot-web/latest
cp -r ../docs/build/html/springboot source/java/spring-boot-web/$RELEASE_VERSION

info "Copying over javadocs"
rm -rf source/java/apidocs
cp -r ../target/site/apidocs source/java/apidocs
cp -r ../target/site/apidocs source/java/apidocs/latest
cp -r ../target/site/apidocs source/java/apidocs/$RELEASE_VERSION

ls -la source/java/apidocs/com/stormpath/spring

git add --all
git commit -m "stormpath-sdk-java release $RELEASE_VERSION"
ls -la source/java/servlet-plugin
git push origin source
