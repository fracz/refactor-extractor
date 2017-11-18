commit 72e076220323e0b50108c6c9a1257cbb59f7eccf
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Jan 26 21:54:07 2014 +0100

    Fixed the AbstractMultiTestRunner so that it supports filters. Filters need to be applied at the level of individual runner that actually drives the execution. This way, it also improves IDE experience as it is possible to run a single test method right from the ide (e.g. launch test when cursor is in the test method).