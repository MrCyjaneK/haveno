#!/bin/bash
set -e
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

cp build/* $ROOT/dist

cd $ROOT/dist/ui
echo 'package ui' > main.go
echo ' ' >> main.go
echo 'import "embed"' >> main.go
echo ' ' >> main.go
echo '//go:embed robots.txt manifest.json logo512.png logo192.png index.html favicon.ico asset-manifest.json static' >> main.go
echo 'var Files embed.FS' >> main.go
