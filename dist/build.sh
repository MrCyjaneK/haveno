#!/bin/bash
set -e
set -x


source $HOME/.cargo/env

cd $(dirname "$0") && cd ..;
ROOT="$(pwd)"
echo "Building in $(pwd)";
cd ../..
if [[ ! -d "/haveno" ]];
then
    echo "WARN: Build directory should be /haveno"
    cp -r /build /haveno # Fix for relative paths 
fi


git clone https://github.com/haveno-dex/haveno-ui-poc
cd haveno-ui-poc
npm install


# NOTE: The code actuall fails with a couple errors like this one:
# TypeScript error in /haveno-ui-poc/src/HavenoDaemon.ts(294,14):
# Expected 1 arguments, but got 0. Did you forget to include 'void' in your type argument to 'Promise'?  TS2794
# 
#     292 |       that._accountClient.openAccount(new OpenAccountRequest().setPassword(password), {password: that._password}, function(err: grpcWeb.RpcError) {
#     293 |         if (err) reject(err);
#   > 294 |         else resolve();
#         |              ^
#     295 |       });
#     296 |     });
#     297 |     return this._awaitAppInitialized(); // TODO: grpc should not return before setup is complete
#
# My, for sure wrong, fix was to change `else resolve()` to `// else resove()`
# This also happened in /haveno-ui-poc/src/HavenoDaemon.ts(744,22), where an
# argument was provided.
# To make sure that I'll provide something that actually builds (and throws runtime errors)
#"Fixed" version of `src/HavenoDaemon.ts` is provided right in this directory.

cp $ROOT/dist/HavenoDaemon.ts src/HavenoDaemon.ts

# Fix: ERR_OSSL_EVP_UNSUPPORTED
NODE_OPTIONS=--openssl-legacy-provider npm run build
mkdir -p $ROOT/dist/ui

cp -r build/* $ROOT/dist/ui
cp -r build/* $ROOT/dist/ui-tauri-pack/haveno/dist # tauri

cd $ROOT
make

cd $ROOT/dist
./download_openjdk.sh

cd $ROOT/dist/ui
echo 'package ui' > main.go
echo ' ' >> main.go
echo 'import "embed"' >> main.go
echo ' ' >> main.go
echo '//go:embed robots.txt manifest.json logo512.png logo192.png index.html favicon.ico asset-manifest.json static' >> main.go
echo 'var Files embed.FS' >> main.go

cd $ROOT/dist
mkdir -p out/target
cd out/target
ROOTFS="$(pwd)"
# Tauri launcher
cd $ROOT/dist/ui-tauri-pack/haveno
npm install
npm run tauri build

# Build launcher
cd $ROOT/dist
go build -v




mkdir -p $ROOTFS/bin
echo "install:" > "$ROOTFS/Makefile"
mv dist $ROOTFS/bin/haveno
echo -e "\tcp bin/haveno \${DESTDIR}/bin/haveno" >> "$ROOTFS/Makefile"
mv envoy $ROOTFS/bin/haveno-envoy
echo -e "\tcp bin/haveno-envoy \${DESTDIR}/bin/haveno-envoy" >> "$ROOTFS/Makefile"
mkdir -p $ROOTFS/etc
cp envoy.yaml $ROOTFS/etc/haveno-envoy.yaml
echo -e "\tcp etc/haveno-envoy.yaml \${DESTDIR}/etc/haveno-envoy.yaml" >> "$ROOTFS/Makefile"
wget https://github.com/haveno-dex/monero/releases/download/testing4/monero-bins-haveno-linux.tar.gz -O - | tar xzv
mv monerod "$ROOTFS/bin/haveno-monerod"
echo -e "\tcp bin/haveno-monerod \${DESTDIR}/bin/haveno-monerod" >> "$ROOTFS/Makefile"
cp ../haveno-seednode "$ROOTFS/bin/haveno-seednode"
sed -i 's/APP_HOME=/#APP_HOME=/g' "$ROOTFS/bin/haveno-seednode"
echo -e "\tcp bin/haveno-seednode \${DESTDIR}/bin/haveno-seednode" >> "$ROOTFS/Makefile"
cp ../haveno-daemon "$ROOTFS/bin/haveno-daemon"
sed -i 's/APP_HOME=/#APP_HOME=/g' "$ROOTFS/bin/haveno-daemon"
echo -e "\tcp bin/haveno-daemon \${DESTDIR}/bin/haveno-daemon" >> "$ROOTFS/Makefile"
# Java libs
mkdir -p $ROOTFS/usr/lib/haveno
cp -r ../lib "$ROOTFS/usr/lib/haveno/lib"
echo -e "\tcp -r usr/lib/haveno \${DESTDIR}/usr/lib/haveno" >> "$ROOTFS/Makefile"
mv monero-wallet-rpc "$ROOTFS/usr/lib/haveno/monero-wallet-rpc"
echo -e "\tcp usr/lib/haveno/monero-wallet-rpc \${DESTDIR}/usr/lib/haveno/monero-wallet-rpc" >> "$ROOTFS/Makefile"
# Java itself
cp -r openjdk "$ROOTFS/usr/lib/haveno/openjdk"
echo -e "\tcp -r usr/lib/haveno/openjdk \${DESTDIR}/usr/lib/haveno/openjdk" >> "$ROOTFS/Makefile"
# Tauri
cp -r ui-tauri-pack/haveno/src-tauri/target/release/bundle/appimage/haveno*.AppImage "$ROOTFS/bin/haveno-frontend"
echo -e "\tcp -r bin/haveno-frontend \${DESTDIR}/bin/haveno-frontend" >> "$ROOTFS/Makefile"
# Icons
mkdir -p "$ROOTFS/usr/share/pixmaps/"
cp haveno.png "$ROOTFS/usr/share/pixmaps/"
echo -e "\tmkdir -p \${DESTDIR}/usr/share/pixmaps/"
echo -e "\tcp usr/share/pixmaps/haveno.png \${DESTDIR}/usr/share/pixmaps/haveno.png"
# Desktop file
mkdir -p "$ROOTFS/usr/share/applications/"
cp haveno.desktop "$ROOTFS/usr/share/applications/"
echo -e "\tmkdir -p \${DESTDIR}/usr/share/applications/"
echo -e "\tcp usr/share/applications/haveno.desktop \${DESTDIR}/usr/share/applications/haveno.desktop"


# Create tar archive
cd "$ROOTFS"
GZIP=-9 tar -czf haveno-bundle.tar.gz *
mv haveno-bundle.tar.gz ..

checkinstall --type=debian \
    --install=no --fstrans=yes \
    -y \
    --pkgname="haveno" \
    --pkgversion="1.0.0" \
    --arch=amd64 \
    --nodoc

mv *.deb ..

AppDir="$ROOT/dist/AppDir"
mkdir "$AppDir"

cd "$ROOT/dist/out/target"

mkdir -p "$AppDir/"{bin,etc,usr/lib,usr/share/applications,usr/share/pixmaps}
make install DESTDIR="$AppDir"
cd "$AppDir"

cp ../haveno.desktop app.desktop
cp ../haveno.png haveno.png
echo '#!/bin/sh' > AppRun
echo 'cd $(dirname "$0")' >> AppRun
echo 'bin/haveno' >> AppRun
chmod +x AppRun

cd ..
appimagetool AppDir
mv *.AppImage out
#cd "$AppDir"
