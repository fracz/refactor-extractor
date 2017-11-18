commit ee33b634a40a39997e561130ab4918c68ba7eaa1
Author: jingwen <jingwen@google.com>
Date:   Tue Sep 26 13:05:48 2017 -0400

    Add integration tests for android_sdk_repository() and android_ndk_repository() for testing invalid directory path attributes, and improve error descriptiveness.

    Moved AndroidRepositoryUtils logic into an abstract AndroidRepositoryFunction that Android{S,N}dkRepositoryFunction extends from.

    Examples of error messages:

    1) Invalid NDK path

    ERROR: Analysis of target '//examples/android/java/bazel:hello_world' failed; build aborted: in target '//external:android/crosstool': no such package '@androidndk//': Could not read RELEASE.TXT in Android NDK: /tmp/RELEASE.TXT (No such file or directory) Unable to read the Android NDK at /tmp, the path may be invalid. Is thepath in android_ndk_repository() or ANDROID_NDK_HOME set correctly? If the path is correct, the contents in the Android NDK directory may have been modified. Please remove the NDK and download it again with the Android SDK manager.

    2) Invalid SDK path

    ERROR: Analysis of target '//examples/android/java/bazel:hello_world' failed; build aborted: no such package '@androidsdk//com.android.support': Expected directory at /tmp/platforms but it is not a directory or it does not exist. Unable to read the Android SDK at /tmp, the path may be invalid. Is the path in android_sdk_repository() or ANDROID_HOME set correctly? If the path is correct, the contents in the Android SDK directory may have been modified. Please remove the SDK and download it again with the Android SDK manager.

    GITHUB: #3740

    RELNOTES: None.
    PiperOrigin-RevId: 170068567