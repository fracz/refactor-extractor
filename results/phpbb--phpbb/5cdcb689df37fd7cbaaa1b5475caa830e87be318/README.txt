commit 5cdcb689df37fd7cbaaa1b5475caa830e87be318
Author: Andy Chase <asperous2@gmail.com>
Date:   Fri Jul 5 14:49:30 2013 -0700

    [ticket/11620] Implemented a provider mock object.

    Due to an auth_refactor, there is a new dependency
    in session.php on phpbb_container and a provider.

    For purposes of testing, implemented a simple one.

    PHPBB3-11620