#!/bin/bash
#IMAGE=envoyproxy/envoy-distroless:v1.20-latest
IMAGE=envoyproxy/envoy-dev:8a2143613d43d17d1eb35a24b4a4a4c432215606
id=$(sudo docker create $IMAGE)
sudo docker cp $id:/usr/local/bin/ - | tar xv
mv bin/envoy .
rm -rf bin
chmod +x envoy
sudo docker rm $id