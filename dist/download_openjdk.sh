#!/bin/bash
mkdir openjdk
cd openjdk
wget https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.14.1%2B1/OpenJDK11U-jdk-shenandoah_x64_linux_11.0.14.1_1.tar.gz

tar xzf OpenJDK11U-jdk-shenandoah_x64_linux_11.0.14.1_1.tar.gz
cd openjdk-11.0.14.1_1
mv * ..
cd ..
rm -rf openjdk-11.0.14.1_1
rm OpenJDK11U-jdk-shenandoah_x64_linux_11.0.14.1_1.tar.gz
