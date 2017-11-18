commit 4c424d61c72af64b4fa1da5e1c52eaebf60fe36e
Author: Hung-ying Tyan <tyanh@google.com>
Date:   Mon Jun 15 11:30:11 2009 +0800

    On the way of refactoring out SingleServerProfile.java.

    + Move mServerName from SingleServerProfile and VpnProfile.
    + Add mSavedUsername to VpnProfile.
    + Keep empty SingleServerProfile to not break the classes that use it.
    + Remove use of SingleServerProfile from VpnService.java.