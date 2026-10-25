#!/usr/bin/env bash
set -euo pipefail

# --- COMPILE SOURCES FIRST ---
"${JAVA_HOME:?set JAVA_HOME to a JDK 11-21}/bin/javac" --release 11 \
  -cp "lib/*" \
  -d out/classes \
  @sources.txt

# --- CONFIG --- (adjust these if needed)
MAIN_CLASS="HTTPServer"              # fully-qualified if in a package, e.g. HTTP.HTTPServer
CLASSES_DIR="out/classes"
LIB_DIR="lib"
MANIFEST="out/manifest-fat.mf"
FAT_JAR="DingNet.jar"

# 0) sanity checks
[[ -d "$CLASSES_DIR" ]] || { echo "Missing $CLASSES_DIR"; exit 1; }
[[ -d "$LIB_DIR" ]] || { echo "Missing $LIB_DIR (put jackson-*.jar etc. here)"; exit 1; }

# 1) manifest (must end with a trailing newline)
mkdir -p "$(dirname "$MANIFEST")"
cat > "$MANIFEST" <<EOF
Manifest-Version: 1.0
Main-Class: $MAIN_CLASS

EOF

# 2) staging dir
rm -rf build-tmp
mkdir -p build-tmp

# 3) unpack all dependency jars into staging
shopt -s nullglob 2>/dev/null || true   # harmless on bash; ignored on zsh
for f in "$LIB_DIR"/*.jar; do
  echo "Unpacking $f"
  unzip -q -o -d build-tmp "$f"
done

# 4) remove signatures & any embedded manifests
find build-tmp/META-INF -type f \( -name '*.SF' -o -name '*.DSA' -o -name '*.RSA' -o -name 'MANIFEST.MF' \) -delete 2>/dev/null || true

# 5) overlay your compiled classes (your classes win)
rsync -a "$CLASSES_DIR"/ build-tmp/

# 6) build the fat jar
JAR_BIN="${JAVA_HOME:-}/bin/jar"
[[ -x "$JAR_BIN" ]] || JAR_BIN="jar"   # fall back to system jar if JAVA_HOME not set
"$JAR_BIN" cfm "$FAT_JAR" "$MANIFEST" -C build-tmp .

echo "Built $FAT_JAR"
echo "Quick check for Jackson inside the jar:"
jar tf "$FAT_JAR" | grep -m1 'com/fasterxml/jackson/databind/ObjectMapper' || echo "Jackson classes not found (check your lib/ jars)."

rm -rf build-tmp

# --- DOCKER CLEANUP AND REBUILD ---
echo "Searching for containers running the 'dingnet' image..."

# Find all container IDs using the 'dingnet' image (running or stopped)
CONTAINERS=$(docker ps -a -q --filter "ancestor=dingnet")

if [[ -n "$CONTAINERS" ]]; then
  echo "Stopping and removing existing containers using the 'dingnet' image..."
  docker stop $CONTAINERS || true
  docker rm $CONTAINERS || true
else
  echo "No containers using the 'dingnet' image found."
fi

# Remove old image if it exists
if docker images dingnet -q > /dev/null 2>&1 && [[ -n "$(docker images dingnet -q)" ]]; then
  echo "Removing old 'dingnet' image..."
  docker rmi -f dingnet || true
fi

# Build new image
echo "Building new Docker image for linux/amd64..."
docker build --platform=linux/amd64 -t dingnet .

echo "✅ Done. New Docker image 'dingnet' built successfully."

