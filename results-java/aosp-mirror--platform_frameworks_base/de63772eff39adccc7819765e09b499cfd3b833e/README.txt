commit de63772eff39adccc7819765e09b499cfd3b833e
Author: Erik Kline <ek@google.com>
Date:   Thu Oct 12 22:16:01 2017 +0900

    Switch to listening for CarrierConfig changes for provisioning rechecks

    This change switches the signal used for provisioning rechecks from:

        ACTION_SIM_STATE_CHANGED

    to:

        ACTION_CARRIER_CONFIG_CHANGED

    Additionally:
        - reexamine carrier config overrides during provisioning re-checks
          (this just was never happening before).
        - refactor shared code out to VersionedBroadcastListener

    Test: as follows
        - built
        - flashed
        - booted
        - runtest frameworks-net
    Bug: 63400667
    Bug: 67755969
    Change-Id: Ib9d222eb7ca0e0dd988a1bd97ab32059189ada2c