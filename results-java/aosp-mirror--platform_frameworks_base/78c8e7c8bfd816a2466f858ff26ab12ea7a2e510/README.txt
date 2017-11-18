commit 78c8e7c8bfd816a2466f858ff26ab12ea7a2e510
Author: Jianzheng Zhou <jianzheng.zhou@freescale.com>
Date:   Sat Feb 8 12:43:07 2014 +0800

    DO NOT MERGE refactor wifi p2p's startDhcpServer function

    Add getTetheredDhcpRanges() interface and call it before calling
    mNwService.startTethering() to update dhcp ranges. This will allow p2p app
    to run well concurrently with other tethering app(e.g. usb tethering).

    Change-Id: I5e8ffeb5d2d396f48b897cd9396f133e25ecca57
    Signed-off-by: Jianzheng Zhou <jianzheng.zhou@freescale.com>