commit 5832c9d58fd5322cd9fecf2a330666f23c0e5bef
Merge: 8148725 3c36596
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 21 11:12:23 2016 -0700

    bug #20001 [Routing] Fixed route generation with fragment defined as defaults (akovalyov)

    This PR was merged into the 3.2-dev branch.

    Discussion
    ----------

    [Routing] Fixed route generation with fragment defined as defaults

    | Q             | A
    | ------------- | ---
    | Branch?       | "master"
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT

    As stated in https://symfony.com/blog/new-in-symfony-3-2-routing-improvements, it should support `_fragment` option as part of `_defaults` of route definition.

    Commits
    -------

    3c36596 [Routing] Fixed route generation with fragment defined as defaults