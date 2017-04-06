commit 94ffc4fab294a90ecb2da8bd11e9c9a7bed69eb4
Merge: 045cbc5 103fd88
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jul 25 08:30:34 2014 +0200

    bug #11469  [BrowserKit] Fixed server HTTP_HOST port uri conversion (bcremer, fabpot)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

     [BrowserKit] Fixed server HTTP_HOST port uri conversion

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #11356
    | License       | MIT
    | Doc PR        | n/a

    See #11356

    Commits
    -------

    103fd88 [BrowserKit] refactor code and fix unquoted regex
    f401ab9 Fixed server HTTP_HOST port uri conversion