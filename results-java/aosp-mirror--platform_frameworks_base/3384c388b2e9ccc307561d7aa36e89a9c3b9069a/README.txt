commit 3384c388b2e9ccc307561d7aa36e89a9c3b9069a
Author: Wei Huang <wei.x.huang@sony.com>
Date:   Fri Apr 21 18:59:31 2017 +0900

    Support RRO for emergency number conversion map

    With current implementation, configurable emergency call number map is
    read from global shared resources object that provides access to only
    system resources(no application resources).
    It means RRO(runtime resource overlay) is NOT supported.

    This patch also applies refactoring to remove function
    isConvertToEmergencyNumberEnabled.

    Bug: 37762325
    Test: Manual
    Change-Id: Ib1672fabbb77880c89f31bf1661b4690e5c8a064