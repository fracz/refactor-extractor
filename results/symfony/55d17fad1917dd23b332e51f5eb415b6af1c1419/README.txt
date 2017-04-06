commit 55d17fad1917dd23b332e51f5eb415b6af1c1419
Merge: b5d0501 8a47b62
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Aug 31 06:03:33 2014 +0200

    minor #11635 [Finder] Fix findertest readability (1emming)

    This PR was submitted for the master branch but it was merged into the 2.3 branch instead (closes #11635).

    Discussion
    ----------

    [Finder] Fix findertest readability

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | part of #11631, #11588
    | License       | MIT

    When running on Ubuntu it is hard to setup an account that allows the `chmod` to downgrade the rights on a directory but is not allow to read from the downgraded directory.

    Commits
    -------

    8a47b62 [Finder] Fix findertest readability