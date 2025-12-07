#!/bin/bash

# Clean and create output directory
rm -rf out_peer
mkdir -p out_peer

# Compile all peer and common java files
javac -cp "lib/*" -d out_peer $(find ./peer -name "*.java") $(find ./common -name "*.java")

# Extract all libraries from lib directory
mkdir -p temp_lib
cd temp_lib
for f in ../lib/*.jar; do
    jar xf "$f"
done
cp -r * ../out_peer/
cd ..
rm -rf temp_lib

# Create manifest file
echo "Main-Class: peer.PeerMain" > manifest_peer.txt

# Create the JAR file
jar cvfm peer.jar manifest_peer.txt -C out_peer .

# Clean up
rm manifest_peer.txt

echo "Peer JAR created successfully: peer.jar"

rm -rf out_peer

