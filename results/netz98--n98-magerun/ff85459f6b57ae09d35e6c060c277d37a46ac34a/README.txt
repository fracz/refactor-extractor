commit ff85459f6b57ae09d35e6c060c277d37a46ac34a
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sat Aug 8 23:49:04 2015 +0200

    [FIX] improve cache:clean test skip

    Non-explainables aren't good for reseaoning, so the "don't know why"
    skippings have been removed and we tried to find out what the concrete
    reasons are.

    As the tests did run but only not for 1.9.x versions, the skip is only
    done for these versions. Also a comment has been left there in case the
    (assumed) flaw has been fixed, the skip will be removed as well.

    Both tests of that testcase are skipped as both tests are affected by
    that problem. For the version check Magento 1 CE and EE is taken into
    account.