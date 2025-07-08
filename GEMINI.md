# Gemini Project Configuration

# NOTE FOR GEMINI:
# When creating git commit messages, be mindful of shell quoting. To include a
# single quote (') in a message, wrap the entire multi-line message in double
# quotes ("). Do not use backticks (`).
# Also, please always ask before committing to git by showing me the proposed commit message. If I approve the message, you should proceed with the commit.
# When creating commit messages, please don't include every tiny detail - make them concise and readable, using the Conventional Commits format. When committing, also do a push.
# Always confirm when you want to run git checkout to revert changes.
# When I reply with "LGTM", it means OK or proceed.

# When asked to check the project or run tests, run all unit and android instrumentation
# tests.

# If you are adding dependencies, please use libs.versions.toml and if the same version
# is used across multiple libraries, use just one version contact with a good name - update the
# name if the use changes. Use names with clarity and consistency. For the version, avoid "version"
# suffix and kebab-style - use caml case.

# When editing any kotlin or java files, please double-check that all imports are at the top of the file.

# After you make changes, please verify that all unit and instrumented tests pass.

# When running tests and attempting to fix failing tests, take into account which tests changed status
# and whether previously failing tests started to pass.
# Whenever running tests, get details of every failing test case so that failure can be better pinpointed. It will also make it clearer when there is progress by fixing a subset of failing test
cases, where the overall test doesn't change status.
# When reporting on test results, list each failing test case individually. This helps track progress even if the overall test suite is still failing.
