#!/bin/bash
set -evx
$HOME/.minecraft/runtime/jre-legacy/linux/jre-legacy/bin/java -Djava.library.path=$HOME/minecraft-libs \
								  -Dminecraft.launcher.brand=minecraft-launcher \
								  -Dminecraft.launcher.version=2.2.5516 \
								  -Dminecraft.client.jar=$HOME/.minecraft/versions/1.8.9/1.8.9.jar \
								  -cp $HOME/.minecraft/libraries/com/mojang/netty/1.7.7/netty-1.7.7.jar:$HOME/.minecraft/libraries/oshi-project/oshi-core/1.1/oshi-core-1.1.jar:$HOME/.minecraft/libraries/net/java/dev/jna/jna/3.4.0/jna-3.4.0.jar:$HOME/.minecraft/libraries/net/java/dev/jna/platform/3.4.0/platform-3.4.0.jar:$HOME/.minecraft/libraries/com/ibm/icu/icu4j-core-mojang/51.2/icu4j-core-mojang-51.2.jar:$HOME/.minecraft/libraries/net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar:$HOME/.minecraft/libraries/com/paulscode/codecjorbis/20101023/codecjorbis-20101023.jar:$HOME/.minecraft/libraries/com/paulscode/codecwav/20101023/codecwav-20101023.jar:$HOME/.minecraft/libraries/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar:$HOME/.minecraft/libraries/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar:$HOME/.minecraft/libraries/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar:$HOME/.minecraft/libraries/io/netty/netty-all/4.0.23.Final/netty-all-4.0.23.Final.jar:$HOME/.minecraft/libraries/com/google/guava/guava/17.0/guava-17.0.jar:$HOME/.minecraft/libraries/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar:$HOME/.minecraft/libraries/commons-io/commons-io/2.4/commons-io-2.4.jar:$HOME/.minecraft/libraries/commons-codec/commons-codec/1.9/commons-codec-1.9.jar:$HOME/.minecraft/libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar:$HOME/.minecraft/libraries/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar:$HOME/.minecraft/libraries/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar:$HOME/.minecraft/libraries/com/mojang/authlib/1.5.21/authlib-1.5.21.jar:$HOME/.minecraft/libraries/com/mojang/realms/1.7.59/realms-1.7.59.jar:$HOME/.minecraft/libraries/org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar:$HOME/.minecraft/libraries/org/apache/httpcomponents/httpclient/4.3.3/httpclient-4.3.3.jar:$HOME/.minecraft/libraries/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar:$HOME/.minecraft/libraries/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar:$HOME/.minecraft/libraries/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar:$HOME/.minecraft/libraries/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar:$HOME/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar:$HOME/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar:$HOME/.minecraft/libraries/tv/twitch/twitch/6.5/twitch-6.5.jar:$HOME/.minecraft/versions/1.8.9/1.8.9.jar \
								  -Xmx2G \
								  -XX:+UnlockExperimentalVMOptions \
								  -XX:+UseG1GC \
								  -XX:G1NewSizePercent=20 \
								  -XX:G1ReservePercent=20 \
								  -XX:MaxGCPauseMillis=50 \
								  -XX:G1HeapRegionSize=32M \
								  -Dlog4j.configurationFile=$HOME/.minecraft/assets/log_configs/client-1.7.xml \
								  net.minecraft.client.main.Main \
								  --username ep1cpr0gamer \
								  --version 1.8.9 \
								  --gameDir $HOME/.minecraft2 \
								  --assetsDir $HOME/.minecraft2/assets \
								  --assetIndex 1.8 \
								  --uuid 9bf95247d0ed4c22877e0ac31532ade7 \
								  --accessToken $1 \
								  --userProperties '{ "preferredLanguage" :  ["en-us" ] }' \
								  --userType microsoft


