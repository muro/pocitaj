---
description: Run all unit and instrumented tests automatically
---

This workflow automates the execution of all tests in the project. It will automatically start the required emulator if no device is connected, run unit tests in parallel with the boot process, and then run all instrumented tests.

1. Ensure your environment is set up (Android SDK paths are correct in `run_all_tests.sh`).
2. Run the test script:
// turbo
```bash
./run_all_tests.sh
```

The script will return a non-zero exit code if any test fails.
