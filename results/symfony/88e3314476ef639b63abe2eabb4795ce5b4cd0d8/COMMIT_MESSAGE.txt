commit 88e3314476ef639b63abe2eabb4795ce5b4cd0d8
Author: Christian Flothmann <christian.flothmann@xabbuh.de>
Date:   Mon Dec 1 20:40:50 2014 +0100

    [Console] improve deprecation warning triggers

    Since the default helper set of the Console `Application` relies on
    the `DialogHelper`, the `ProgressHelper` and the `TableHelper`, there
    must be a way to not always trigger `E_USER_DEPRECATION` errors when
    one of these helper is used.