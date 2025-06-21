#!/bin/bash

# === CONFIGURATION ===
PLUGIN_YML="src/main/resources/paper-plugin.yml"
SERVER_DIR="./server"
PAPER_BUILD_URL="https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/232/downloads/paper-1.21.4-232.jar"
PAPER_JAR_NAME="paper.jar"
PLUGIN_JAR_PATH="build/libs/${PLUGIN_NAME}*.jar"

# === STEP 0: Extract plugin name and version ===
if [ ! -f "$PLUGIN_YML" ]; then
    echo "ERROR: $PLUGIN_YML not found!"
    exit 1
fi

PLUGIN_NAME=$(grep '^name:' "$PLUGIN_YML" | sed 's/name:[[:space:]]*//')
PLUGIN_VERSION=$(grep '^version:' "$PLUGIN_YML" | sed 's/version:[[:space:]]*//')

if [ -z "$PLUGIN_NAME" ] || [ -z "$PLUGIN_VERSION" ]; then
    echo "ERROR: Could not parse plugin name or version from $PLUGIN_YML"
    exit 1
fi

PLUGIN_JAR="${PLUGIN_NAME}-${PLUGIN_VERSION}.jar"
PLUGIN_JAR_PATH="build/libs/${PLUGIN_JAR}"

# === STEP 1: Build the Plugin ===
echo ">> Building the Gradle project..."
./gradlew build || { echo "Gradle build failed!"; exit 1; }

# === STEP 2: Set Up the Server If It Doesn't Exist ===
if [ ! -f "${SERVER_DIR}/${PAPER_JAR_NAME}" ]; then
    echo ">> Setting up Paper server..."
    mkdir -p "${SERVER_DIR}"
    curl -o "${SERVER_DIR}/${PAPER_JAR_NAME}" -L "${PAPER_BUILD_URL}" || { echo "Failed to download Paper server."; exit 1; }
    echo ">> Accepting EULA..."
    echo "eula=true" > "${SERVER_DIR}/eula.txt"
fi

# === STEP 3: Copy Plugin to Plugins Folder ===
echo ">> Copying plugin jar (${PLUGIN_JAR}) to server plugins folder..."
mkdir -p "${SERVER_DIR}/plugins"
cp "${PLUGIN_JAR_PATH}" "${SERVER_DIR}/plugins/" || { echo "Plugin JAR not found: ${PLUGIN_JAR_PATH}"; exit 1; }

# === STEP 4: Start the Server ===
echo ">> Starting the server..."
cd "${SERVER_DIR}" || exit
java -Xms1G -Xmx1G -jar "${PAPER_JAR_NAME}" nogui
