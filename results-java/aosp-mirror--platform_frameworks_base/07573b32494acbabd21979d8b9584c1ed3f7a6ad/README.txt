commit 07573b32494acbabd21979d8b9584c1ed3f7a6ad
Author: Irfan Sheriff <isheriff@google.com>
Date:   Fri Jan 27 21:00:19 2012 -0800

    Improve Wi-Fi hand-off

    When Wi-fi connects at L2 layer, the beacons reach and the device
    can maintain a connection to the access point, but the application
    connectivity can be flaky (due to bigger packet size exchange).

    We now use Watchdog to monitor the quality of the last hop on
    Wi-Fi using signal strength and ARP connectivity as indicators
    to decide if the link is good enough to switch to Wi-Fi as the uplink.

    ARP pings are useful for link validation but can still get through
    when the application traffic fails to go through and thus not best indicator
    real packet loss since they are tiny packets (28 bytes) and have
    much low chance of packet corruption than the regular data
    packets.

    Signal strength and ARP used together ends up working well in tests.
    The goal is to switch to Wi-Fi after validating ARP transfer
    and RSSI and then switching out of Wi-Fi when we hit a low
    signal strength threshold and waiting until the signal strength
    improves and validating ARP transfer.

    Change-Id: Ica593291ec7772da892f03cf45b649635b730c47