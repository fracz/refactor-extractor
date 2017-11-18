commit 3d3308d3e476f62664a9c3c31e6bc95bc879e49e
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Aug 23 17:41:47 2016 -0700

    Moar test cases for NetworkPolicyManagerServiceTest.

    Before refactoring restrict-background into a UID policy, it's important
    to make sure apps receive the proper ACTION_RESTRICT_BACKGROUND_CHANGED
    intents.

    BUG: 28791717

    Test: m -j32 FrameworksServicesTests && adb install -r -g ${ANDROID_PRODUCT_OUT}/data/app/FrameworksServicesTests/FrameworksServicesTests.apk && adb shell am instrument -e class "com.android.server.NetworkPolicyManagerServiceTest" -w "com.android.frameworks.servicestests/android.support.test.runner.AndroidJUnitRunner"

    Change-Id: I32b2e36750ce4640b57d9b1d29dc53ec641456fa