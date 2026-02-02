#!/bin/bash

# Configuration
AVD_NAME="Medium_Phone_API_36.1"

# Resolve Android SDK path
if [ -z "$ANDROID_HOME" ]; then
    if [ -f "local.properties" ]; then
        ANDROID_HOME=$(grep '^sdk.dir=' local.properties | cut -d'=' -f2-)
    fi
fi

if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME is not set and could not be found in local.properties"
    exit 1
fi

ADB_PATH="$ANDROID_HOME/platform-tools/adb"
EMULATOR_PATH="$ANDROID_HOME/emulator/emulator"

echo "Checking for running Android devices..."
# Get first running device/emulator
DEVICE_ID=$($ADB_PATH devices | grep -v "List" | grep "device$" | head -n 1 | cut -f 1)

if [ -z "$DEVICE_ID" ]; then
    echo "No running device found. Starting emulator: $AVD_NAME..."
    $EMULATOR_PATH -avd "$AVD_NAME" -no-snapshot-load -no-boot-anim &
    DEVICE_ID="emulator-5554"
fi

echo "Waiting for device $DEVICE_ID to boot (unit tests will run in parallel)..."
# Run unit tests in parallel while waiting for boot
./gradlew testDebugUnitTest &
UNIT_TEST_PID=$!

BOOT_COMPLETED=0
while [ $BOOT_COMPLETED -eq 0 ]; do
    BOOT_STATUS=$($ADB_PATH -s "$DEVICE_ID" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    if [ "$BOOT_STATUS" = "1" ]; then
        BOOT_COMPLETED=1
        echo "Device $DEVICE_ID is ready."
    else
        sleep 5
    fi
done

# Wait for unit tests to finish
wait $UNIT_TEST_PID
UNIT_TEST_EXIT_CODE=$?

# Run instrumented tests
echo "Running instrumented tests..."
./gradlew connectedDebugAndroidTest
INSTRUMENTED_TEST_EXIT_CODE=$?

# Summary
echo "------------------------------------------------"
if [ $UNIT_TEST_EXIT_CODE -eq 0 ]; then
    echo "Unit Tests: PASSED"
else
    echo "Unit Tests: FAILED"
fi

if [ $INSTRUMENTED_TEST_EXIT_CODE -eq 0 ]; then
    echo "Instrumented Tests: PASSED"
else
    echo "Instrumented Tests: FAILED"
fi
echo "------------------------------------------------"

if [ $UNIT_TEST_EXIT_CODE -eq 0 ] && [ $INSTRUMENTED_TEST_EXIT_CODE -eq 0 ]; then
    exit 0
else
    exit 1
fi
