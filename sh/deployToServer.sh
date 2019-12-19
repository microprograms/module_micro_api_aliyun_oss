#!/bin/bash

mvn clean package

ssh root@scratch.flyingbears.cn "rm /opt/osgi-framework-launcher/bundle/module_micro_api_aliyun_oss-*.jar"
scp target/module_micro_api_aliyun_oss-*.jar root@scratch.flyingbears.cn:/opt/osgi-framework-launcher/bundle/
ssh root@scratch.flyingbears.cn "cd /opt/osgi-framework-launcher/; sh bin/restart.sh"