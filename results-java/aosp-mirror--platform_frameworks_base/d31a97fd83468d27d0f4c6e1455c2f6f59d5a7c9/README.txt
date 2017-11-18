commit d31a97fd83468d27d0f4c6e1455c2f6f59d5a7c9
Author: Felipe Leme <felipeal@google.com>
Date:   Fri May 6 14:53:50 2016 -0700

    Fixed connectivity state in some restricted network scenarios.

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

    Another scenario that is not properly handled is when a UID whitelisted
    for Data Saver is brought back to foreground: although the proper
    firewall rules are set, CS is not notified, and the apps state would be
    DISCONNECTED / BLOCKED.

    This CL introduces many changes that fix this issue:

    - Fixed updateRuleForRestrictBackgroundLocked() to invoke
      onUidRulesChanged() when the Battery Saver status changed.
    - Fixed updateRuleForRestrictBackgroundLocked() to invoke
      onUidRulesChanged() when an app whitelisted for Data Saver is brought
      back to the foreground.
    - Added a new API (onRestrictPowerChanged() and getRestrictPower())
      to notify external services about Battery Saver mode changes.
    - Fixed CS logic to properly handle the Battery Saver changes.

    Externally to this change, the CTS tests were also improved to verify
    the apps get the proper connection state; they can be verified running:

    cts-tradefed run commandAndExit cts -m CtsHostsideNetworkTests \
        -t com.android.cts.net.HostsideRestrictBackgroundNetworkTests

    BUG: 28521946

    Change-Id: I8eaccd39968eb4b8c6b34f462fbc541e5daf55f1