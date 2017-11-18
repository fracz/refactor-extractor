commit 1654b1d11c3f4195d6f691bd1b6eb5aa672ffc94
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue May 24 11:50:31 2016 +0900

    Fix default network transition event recording

    When disconnecting from a default network X and falling back on another
    connected network Y as the new default, ConnectivityService was
    attempting to record this event as a X -> Y "atomic" transition.

    In practice the default network connectivity is actually lost and
    recovering default network takes some non-zero time.

    This patch changes the event recording to always record disconnection as
    X -> 0 events. At the same time, if there is a fallback network that is
    elected as the new default ConnectivityService will also record a 0 -> Y
    event.

    This patch also improves pretty-printing of DefaultNetworkEvent.

    Extract from $ adb shell dumpsys connectivity_metrics_logger --events
    17:51:00.086: DefaultNetworkEvent(0 -> 100:CELLULAR)
    17:51:25.232: DefaultNetworkEvent(100:IPv4 -> 101:WIFI) # wifi goes on
    17:51:44.064: DefaultNetworkEvent(101:DUAL -> 0)        # wifi goes off
    17:51:44.187: DefaultNetworkEvent(0 -> 100:CELLULAR)

    Bug: 28204408
    Change-Id: I63252633235bf6ba833b9ac431a80dda75a93e67