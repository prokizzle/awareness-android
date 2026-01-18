fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build_debug

```sh
[bundle exec] fastlane android build_debug
```

Build debug APK

### android build_release

```sh
[bundle exec] fastlane android build_release
```

Build release APK

### android build_aab

```sh
[bundle exec] fastlane android build_aab
```

Build release AAB for Play Store

### android test

```sh
[bundle exec] fastlane android test
```

Run unit tests

### android lint

```sh
[bundle exec] fastlane android lint
```

Run lint checks

### android deploy_internal

```sh
[bundle exec] fastlane android deploy_internal
```

Deploy to Play Store internal track

### android deploy_beta

```sh
[bundle exec] fastlane android deploy_beta
```

Deploy to Play Store beta track

### android deploy_production

```sh
[bundle exec] fastlane android deploy_production
```

Deploy to Play Store production

### android bump_version

```sh
[bundle exec] fastlane android bump_version
```

Increment version code

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
