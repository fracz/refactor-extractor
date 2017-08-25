commit 01ef46a510c067d7699a3495ca533aee5f112ae4
Author: Oleg Pudeyev <oleg@bsdpower.com>
Date:   Tue Apr 20 06:53:52 2010 -0400

    [ticket/9061] Fixed a race condition in queue locking.

    Changed queue locking to cover all queue file operations,
    in particular the check for queue file existince and
    inclusion of queue file must be done under one lock.

    Also refactored queue locking and unlocking into separate
    methods.

    PHPBB3-9061