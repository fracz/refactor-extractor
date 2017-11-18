commit 515fabce96aa904092c6b0c80bf5f58055f54b92
Author: dgolitsyn <golitsyn.dima@gmail.com>
Date:   Fri May 26 20:02:09 2017 +0400

    Server selector improvement (#4315)

    * Do not re-create prioritized servers on each call in server selector and extend TierSelectorStrategy interface with a method to pick multiple elements at once

    * Fix compilation