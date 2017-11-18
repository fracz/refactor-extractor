commit 46b451fa7da6c0323e80616409cf467d5b1fd01f
Author: Felipe Leme <felipeal@google.com>
Date:   Fri Aug 19 08:46:17 2016 -0700

    Refactored whitelist restrict background uids.

    On Android N, the list of UIDs whitelisted was kept in a separate array
    and XML tag, while in reality it should be a new UID
    policy (POLICY_ALLOW_METERED_BACKGROUND).

    This change refactors NetworkPolicyManagerService to use the new UID
    policy, although without removing any of its existing methods (but
    marking them as deprecated).

    Test: m -j32 FrameworksServicesTests && adb install -r -g ${ANDROID_PRODUCT_OUT}/data/app/FrameworksServicesTests/FrameworksServicesTests.apk && adb shell am instrument -e class "com.android.server.NetworkPolicyManagerServiceTest" -w "com.android.frameworks.servicestests/android.support.test.runner.AndroidJUnitRunner"

    Test: cts-tradefed run commandAndExit cts --skip-device-info --skip-system-status-check com.android.compatibility.common.tradefed.targetprep.NetworkConnectivityChecker --skip-preconditions -m CtsHostsideNetworkTests --abi armeabi-v7a -t com.android.cts.net.HostsideRestrictBackgroundNetworkTests

    BUG: 28791717

    Change-Id: I39869efda554ca0b736dd2380e439474f91dfbe6