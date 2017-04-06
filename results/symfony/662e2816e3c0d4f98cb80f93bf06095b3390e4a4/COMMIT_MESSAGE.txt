commit 662e2816e3c0d4f98cb80f93bf06095b3390e4a4
Merge: 5c7cb56 30aa4e9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jun 5 15:57:41 2015 +0200

    minor #14746 [Config] Improved duplicated code in FileLocator (dosten)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Config] Improved duplicated code in FileLocator

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT

    This PR improves a duplicate check prepending the current path (if exists) to the list of paths.

    Commits
    -------

    30aa4e9 Improved duplicated code in FileLocator