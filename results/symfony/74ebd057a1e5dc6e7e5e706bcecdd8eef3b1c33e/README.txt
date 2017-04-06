commit 74ebd057a1e5dc6e7e5e706bcecdd8eef3b1c33e
Merge: dc1ff89 fb2bb65
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Feb 21 14:47:46 2012 +0100

    merged branch tna/session-cache-limiter (PR #3400)

    Commits
    -------

    fb2bb65 [HttpFoundation] Fix session.cache_limiter is not set correctly

    Discussion
    ----------

    [HttpFoundation] Fix session.cache_limiter is not set correctly

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    Fixes a regression after the session refactoring where extra cache control http headers are sent.

    This was previously handled by [calling session_cache_limiter(false) in NativeSessionStorage](https://github.com/symfony/symfony/blob/2.0/src/Symfony/Component/HttpFoundation/SessionStorage/NativeSessionStorage.php#L81)

    ---------------------------------------------------------------------------

    by drak at 2012-02-21T12:23:48Z

    @fabpot - this code can be merged imo.