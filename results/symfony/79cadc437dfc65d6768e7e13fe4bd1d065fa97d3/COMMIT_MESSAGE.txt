commit 79cadc437dfc65d6768e7e13fe4bd1d065fa97d3
Merge: a00d7dc 75db8eb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Apr 20 17:48:56 2013 +0200

    merged branch fabpot/request (PR #7734)

    This PR was merged into the master branch.

    Discussion
    ----------

    Refactored tests of Request::getTrustedProxies()

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    This PR reorganizes the tests for the request trusted proxies as they were unreadable. In the process, I've also removed obsolete stuff and changed the order returned by `Request::getClientIps()` (not a BC break as this method was added in 2.3 -- see #7612).

    Commits
    -------

    75db8eb [HttpFoundation] changed the order of IP addresses returned by Request::getClientIps()
    deccb76 [HttpFoundation] refactored trusted proxies tests to make them easier to understand and change
    1af9e5e [Request] removed obsolete proxy setting in tests
    168b8cb [HttpFoundation] removed obsolete request property