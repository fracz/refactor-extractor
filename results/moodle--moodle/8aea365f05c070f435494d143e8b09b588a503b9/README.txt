commit 8aea365f05c070f435494d143e8b09b588a503b9
Author: David Monllao <david.monllao@gmail.com>
Date:   Tue Jul 9 16:37:17 2013 +0200

    MDL-40315 behat: Performance improvement

    We look for exceptions after each step, this patch
    includes a pre-checking query to avoid multiple queries
    for each step when most of the time they are not necessary.