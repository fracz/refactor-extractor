commit 0db39fccaefa6f92c33841af683d6405c7341ae5
Author: andrepereiradasilva <andrepereiradasilva@users.noreply.github.com>
Date:   Sun Sep 18 16:39:27 2016 +0100

    [JAccess] Improve ACL asset preloading performance/memory consumption (#12028)

    * improve component asset preload speed

    * Update user.php

    * Update access.php

    * Update access.php

    * Update access.php

    * Update access.php

    * not convinced yet, but revert changes in juser

    * make sure that components are always preloaded

    * only a logged users (with user id) can be root.