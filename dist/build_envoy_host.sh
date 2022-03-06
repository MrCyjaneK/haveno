#!/bin/bash
git clone https://github.com/envoyproxy/envoy
cd envoy
sudo ./ci/run_envoy_docker.sh './ci/do_ci.sh bazel.release'