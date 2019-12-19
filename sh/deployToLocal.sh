#!/bin/bash

mvn clean package

rm ~/microprograms/osgi-framework-launcher/bundle/module_micro_api_aliyun_oss-*.jar
cp target/module_micro_api_aliyun_oss-*.jar ~/microprograms/osgi-framework-launcher/bundle/

cd ~/microprograms/osgi-framework-launcher/
sh bin/restart.sh