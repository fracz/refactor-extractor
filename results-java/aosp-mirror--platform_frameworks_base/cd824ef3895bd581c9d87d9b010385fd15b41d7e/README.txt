commit cd824ef3895bd581c9d87d9b010385fd15b41d7e
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Tue Feb 7 11:25:14 2017 -0800

    Reconcile apps in 2 phases

    During boot app data folders are reconciled in 2 phases:
     - in the constructor only core apps are reconciled. prepareAppData
       for remaining apps is deferred and run on a separate thread (phase 2)
     - Phase 2 must finish before third-party apps can start

    Also moved GC to final stages of system server init. GC alone takes ~200 ms.

    Overall boot time improvement: ~1 second

    Before:
    02-17 18:33:33 D/BaseBootTest: successive-boot :
    28835.0,29638.0,30205.0,29793.0,29752.0,28228.0,30125.0,28983.0,28487.0,28865.0,
    02-17 18:33:33 D/BaseBootTest: successive-boot_avg : 29291.1
    02-17 18:33:33 D/BaseBootTest:
    SystemServerTiming_StartPackageManagerService :
    3150.0,3615.0,3515.0,3495.0,3814.0,3158.0,3746.0,3274.0,3222.0,3607.0,
    02-17 18:33:33 D/BaseBootTest:
    SystemServerTiming_StartPackageManagerService_avg : 3459.6
    02-17 18:33:33 D/BaseBootTest: SystemServerTiming_StartServices :
    8244.0,8863.0,9035.0,9832.0,8998.0,8096.0,8719.0,8209.0,8279.0,8754.0,
    02-17 18:33:33 D/BaseBootTest: SystemServerTiming_StartServices_avg :
    8702.9

    After:
    02-17 17:59:51 D/BaseBootTest: successive-boot :
    27711.0,27607.0,28408.0,28968.0,28397.0,28063.0,27885.0,28483.0,27917.0,29317.0,
    02-17 17:59:51 D/BaseBootTest: successive-boot_avg : 28275.6
    02-17 17:59:51 D/BaseBootTest:
    SystemServerTiming_StartPackageManagerService :
    2467.0,2489.0,2369.0,2548.0,2647.0,2523.0,2497.0,2553.0,2482.0,2657.0,
    02-17 17:59:51 D/BaseBootTest:
    SystemServerTiming_StartPackageManagerService_avg : 2523.2
    02-17 17:59:51 D/BaseBootTest: SystemServerTiming_StartServices :
    7686.0,7538.0,7598.0,7869.0,7884.0,7950.0,7971.0,8370.0,7696.0,7885.0,
    02-17 17:59:51 D/BaseBootTest: SystemServerTiming_StartServices_avg :
    7844.7

    Test: manual
    Bug: 28750609
    Change-Id: I3543ef577af1365394775318e40907584ddbe950