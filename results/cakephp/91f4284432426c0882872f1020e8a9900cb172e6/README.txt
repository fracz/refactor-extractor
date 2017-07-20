commit 91f4284432426c0882872f1020e8a9900cb172e6
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Sun May 18 18:47:28 2014 +0200

    Removing the static keyword out of AuthComponent

    Now that sessions are no static anymore, maintaining this feature was
    near impossible. On the other hand, I always thought this was an ugly
    hack that was nice to have in the beginning but a nightmare to test
    and refactor