FROM debian:stable

# Haveno depends
RUN apt update -y && \
    apt install -y make wget git git-lfs openjdk-11-jdk

# Golang - for the launcher
RUN rm -rf /usr/local/go; \
    wget https://go.dev/dl/go1.17.8.linux-amd64.tar.gz -O - | tar -C /usr/local -xz && \
    ln -s /usr/local/go/bin/go /bin/go

# UI depends
# RUN apt install -y protobuf-compiler
# Nope, we can't install it from the repo
# because the version is not recent enough.
RUN apt install -y autoconf automake libtool make g++ unzip && \
    mkdir -p /tmp/build/protobuf && cd /tmp/build/protobuf && \
    git clone https://github.com/protocolbuffers/protobuf.git . && \
    git submodule update --init --recursive && \
    ./autogen.sh && \
    ./configure && \
    make -j$(nproc) && \
    make check && \
    make install && \
    ldconfig && \
    wget https://github.com/grpc/grpc-web/releases/download/1.3.1/protoc-gen-grpc-web-1.3.1-linux-x86_64 -O /bin/protoc-gen-grpc-web && \
    chmod +x /bin/protoc-gen-grpc-web

# NodeJS

RUN wget -O - https://deb.nodesource.com/setup_17.x | bash
    apt install -y nodejs
