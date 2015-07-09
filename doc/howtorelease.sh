#!/bin/bash

if [ "$#" != "1" ]; then
  echo "Usage $0 <version in x.y.z form>"
  exit 1
fi

VERSION=$1

if [[ ! ${VERSION} =~ ^[0-9]\.[0-9]\.[0-9]$ ]]; then
  echo "Usage $0 <version in x.y.z form>"
  exit 1
fi

VERSION_MINUS_DOTS=$(echo ${VERSION} | sed s/\\.//g)

echo ${VERSION_MINUS_DOTS}

cat $(dirname $0)/HOW_TO_RELEASE.txt | sed s/x\\.y\\.z/${VERSION}/g | sed s/xyz/${VERSION_MINUS_DOTS}/g

