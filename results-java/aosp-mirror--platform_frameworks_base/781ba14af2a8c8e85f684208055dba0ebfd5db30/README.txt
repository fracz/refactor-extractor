commit 781ba14af2a8c8e85f684208055dba0ebfd5db30
Author: Felipe Leme <felipeal@google.com>
Date:   Mon May 9 16:24:48 2016 -0700

    Fixed connectivity state in some power saving scenarios.

    NetworkPolicyManagerService (NPMS) manages 4 type of network restriction
    when apps are running on background:

    - Data Saver Mode (data usage restriction on metered-networks)
    - Battery Saver Mode (power restriction on all networks)
    - Doze Mode (power restriction on all networks)
    - App Idle (power restriction on all networks)

    These restrictions affects 2 parts of the system:

    - Internal framework state on NPMS which is propagated to other internal
      classes.
    - External firewall rules (managed by netd).

    Although each of the power-related restrictions have their own external firewall
    rules, internally apps are whitelisted to them through the same
    whitelist, and the current code is only updating the internal state (and
    notifying the internal listeners) when Battery Saver Mode is on.

    As a consequence of this problem, there are scenarios where an app
    correctly does not have internet access (because the firewall rules are
    properly set), but the NetworkInfo state returns the wrong state (like
    CONNECTED / CONNECTED).

    This CL fixes this problem by splitting the power-related logic from
    updateRulesForRestrictBackgroundLocked() into its own
    method (updateRulesForPowerRestrictionsLocked()), and making sure such
    method is called whenever the firewall rules are updated.

    Externally to this change, the CTS tests were also improved to verify
    the apps get the proper connection state; it can be verified by running:

    cts-tradefed run commandAndExit cts -m CtsHostsideNetworkTests \
        -t com.android.cts.net.HostsideRestrictBackgroundNetworkTests

    BUG: 28521946
    Change-Id: Id5187eb7a59c549ef30e2b17627ae2d734afa789