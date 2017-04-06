commit 613910bc9fa272cb7b2cf51a0935ffbdd1cbf404
Merge: 66c99a0 d9ac571
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Oct 19 13:54:32 2015 +0200

    bug #16177 [HttpFoundation] Fixes /0 subnet handling in IpUtils (ultrafez)

    This PR was squashed before being merged into the 2.3 branch (closes #16177).

    Discussion
    ----------

    [HttpFoundation] Fixes /0 subnet handling in IpUtils

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #16055
    | License       | MIT
    | Doc PR        | Not needed

    Fixes bug #16055. For IP addresses with CIDR subnet length 0, the IP address must be valid - IPs with subnet masks greater than zero are implicitly validated due to the use of `ip2long` and `substr_compare` (although it's not particularly robust - there could be some future work to improve this here).

    Commits
    -------

    d9ac571 [HttpFoundation] Fixes /0 subnet handling in IpUtils