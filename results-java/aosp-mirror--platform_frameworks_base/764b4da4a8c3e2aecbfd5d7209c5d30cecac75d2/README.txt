commit 764b4da4a8c3e2aecbfd5d7209c5d30cecac75d2
Author: Daniel Sandler <dsandler@google.com>
Date:   Tue Aug 24 16:24:35 2010 -0400

    System bar improvements.

    - Updated artwork for buttons and signal/battery meters.
    - Layout change: meters on either side of clock
    - RSSI for mobile data. The algorithm is more or less the
      same as the one used in the phone status bar. Note that
      mobile data is only shown if Wi-Fi is unavailable.

    Bug: 2924643
    Change-Id: Idaa0c52422db4a63616475bde96626d1953830b4