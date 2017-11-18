commit 46c4fc357ce8e686b6e7c2adda573e9870a8221f
Author: Felipe Leme <felipeal@google.com>
Date:   Wed May 4 09:21:43 2016 -0700

    Refactored NetworkPolicyManagerService mUidRules.

    NetworkPolicyManagerService (NMPS) keeps an internal list of uid
    rules (mUidRules) for network restrictions, and when these rules
    changes it needs to notify external listeners (such as
    ConnectivityService / CS).

    Prior to Android N, both Data Saver mode (the feature previously known
    as "Restrict Baground Data") and Battery Save mode used the same set of
    firewall rules to implement their restrictions: when Battery Saver mode
    NPMS would mark all networks as metered and set the proper firewall
    rules externally.

    Recently, these 2 modes were split in 2 distinct firewall rules and
    NMPS.updateRuleForRestrictBackgroundLocked() was changed to update
    the mUidRules logic based on the Data Saver firewall (since the Battery
    Saver firewall changes are handled externally, on
    updateRuleForRestrictPowerLocked()). As such, CS was not notified when
    the power-related changes were made, which would cause apps to get a
    state of CONNECTED / CONNECTED when querying its active connection.

    This change refactores the mUidRules to use bitmasks, in preparation for
    another change that will fix the issue.

    It also fixes a minor bug that was preventing removed packages to be
    removed from the whitelist.

    BUG: 28521946
    Change-Id: I9f0e1509a6192cad403f740c1cd76a6b7dab7d26