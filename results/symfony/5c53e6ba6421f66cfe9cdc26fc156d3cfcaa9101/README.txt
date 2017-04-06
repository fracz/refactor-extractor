commit 5c53e6ba6421f66cfe9cdc26fc156d3cfcaa9101
Merge: 79cadc4 7b32794
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Apr 20 17:53:03 2013 +0200

    merged branch fabpot/trusted-proxies (PR #7735)

    This PR was merged into the master branch.

    Discussion
    ----------

    [HttpFoundation][FrameworkBundle] Add CIDR notation support in trusted proxy list

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #7312, #7262
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#2287

    Should be rebased once #7734 is merged.

    Commits
    -------

    7b32794 [HttpFoundation] updated CHANGELOG
    e7c1696 [HttpFoundation] refactored code to avoid code duplication
    1695067 [HttpFoundation] added some unit tests for ranges of trusted IP addresses
    811434f Allow setting trusted_proxies using CIDR notation
    ddc9e38 Modify Request::getClientIp() to use IpUtils::checkIp()