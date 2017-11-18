commit 03f9029bed18338eaca0741eb95649cffb0f1874
Author: Felipe Leme <felipeal@google.com>
Date:   Thu Sep 8 18:10:32 2016 -0700

    Fixed mRestrictBackgroundWhitelistRevokedUids usage.

    mRestrictBackgroundWhitelistRevokedUids used to be set on
    removeRestrictBackgroundWhitelistedUidUL(), which has been refactored
    into setUidPolicy().

    Fixes: 28791717

    Test: m -j32 FrameworksServicesTests && adb install -r -g ${ANDROID_PRODUCT_OUT}/data/app/FrameworksServicesTests/FrameworksServicesTests.apk && adb shell am instrument -e class "com.android.server.NetworkPolicyManagerServiceTest" -w "com.android.frameworks.servicestests/android.support.test.runner.AndroidJUnitRunner"

    Change-Id: I097fddd236bf279890a8f466927fdc330360477f