commit 507d11c9353666a75fee014565f900825a907691
Author: Andrew Scull <ascull@google.com>
Date:   Wed May 3 17:19:01 2017 +0100

    Move LockSettingsService into locksettings package.

    This service now has a large number of support classes so move them into
    their own package to keep things tidy and easier to refactor.

    Bug: 37090873
    Test: runtest frameworks-services -c com.android.server.locksettings.LockSettingsServiceTests
    Test: runtest frameworks-services -c com.android.server.locksettings.LockSettingsShellCommandTest
    Test: runtest frameworks-services -c com.android.server.locksettings.SyntheticPasswordTests
    Change-Id: Ic3cd00e6565749defd74498a3491c3d9b914ad90