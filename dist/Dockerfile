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
    make check -j$(nproc) && \
    make install && \
    ldconfig && \
    wget https://github.com/grpc/grpc-web/releases/download/1.3.1/protoc-gen-grpc-web-1.3.1-linux-x86_64 -O /bin/protoc-gen-grpc-web && \
    chmod +x /bin/protoc-gen-grpc-web

# NodeJS

RUN wget -O - https://deb.nodesource.com/setup_17.x | bash && \
    apt install -y nodejs

# Tauri
#RUN apt install -y libwebkit2gtk-4.0-dev \
#    build-essential \
#    curl \
#    wget \
#    libssl-dev \
#    libgtk-3-dev \
#    libappindicator3-0.1-cil-dev \
#    librsvg2-dev
# We don't need nvm, we already have latest nodejs installed.
#RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.2/install.sh | bash

#RUN wget -O /bin/rustup.sh https://sh.rustup.rs && \
#    chmod +x /bin/rustup.sh && \
#    rustup.sh -y

# Envoy
# > Bazel
#RUN wget -O /usr/local/bin/bazel https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-$([ $(uname -m) = "aarch64" ] && echo "arm64" || echo "amd64") && \
#    sudo chmod +x /usr/local/bin/bazel && \
#    apt install -y autoconf automake cmake \
#        curl libtool make ninja-build patch \
#        python3-pip unzip virtualenv

# nghttp2
# ...

# .deb packaging
RUN apt install -y checkinstall

# Tauri.studio

RUN apt install -y libwebkit2gtk-4.0-dev build-essential curl wget libssl-dev libgtk-3-dev librsvg2-dev

RUN wget -O /bin/rustup.sh https://sh.rustup.rs && \
    chmod +x /bin/rustup.sh && \
    rustup.sh -y

# appimage builder
RUN apt install -y python3-pip python3-setuptools patchelf desktop-file-utils libgdk-pixbuf2.0-dev fakeroot strace fuse && \
    wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage -O /opt/appimagetool && \
    cd /opt/; chmod +x appimagetool; sed -i 's|AI\x02|\x00\x00\x00|' appimagetool; ./appimagetool --appimage-extract && \
    mv /opt/squashfs-root /opt/appimagetool.AppDir && \
    ln -s /opt/appimagetool.AppDir/AppRun /usr/local/bin/appimagetool && \
    pip3 install appimage-builder


# Cleanup

RUN rm -rf /var/lib/apt/lists/*