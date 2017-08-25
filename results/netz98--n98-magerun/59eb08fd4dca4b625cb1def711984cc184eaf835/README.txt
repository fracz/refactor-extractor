commit 59eb08fd4dca4b625cb1def711984cc184eaf835
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sun May 31 20:45:34 2015 +0200

    fixed and improved getcwd()

    as the Exec class has been created, the allowance of ::run() (previously
    exec) had to be wrapped, too. this has been done and old allowance checks
    updated to the new API.

    OperationSystem has got a getcwd() method now, moving existing logic into
    it (and fixing it for linux and windows).