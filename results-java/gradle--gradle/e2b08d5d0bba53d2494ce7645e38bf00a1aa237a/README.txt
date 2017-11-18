commit e2b08d5d0bba53d2494ce7645e38bf00a1aa237a
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Sep 12 22:26:46 2011 +0200

    GRADLE-1776 Fixed the issue with jna library not found. Now we will assume that we're not connected to the terminal in case jna is not found. Terminal-detecting logic is needed to have cool colors in the console. Extracted out some jna hackiness to a separate class. This needs further refactorings as we decided to put all native stuff in a separate module.