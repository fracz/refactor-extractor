commit 36df0a7d9b507c46dd785191f2eeb0086fe6ad0a
Merge: bbbb079 f1f9754
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jan 14 17:21:11 2016 +0100

    bug #17370 [HttpFoundation][Cookie] Cookie DateTimeInterface fix (wildewouter)

    This PR was squashed before being merged into the 2.3 branch (closes #17370).

    Discussion
    ----------

    [HttpFoundation][Cookie] Cookie DateTimeInterface fix

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    I came across an issue with expiration times on cookies. They were not working with DateTimeImmutable but only the DateTime implementation itself. I refactored this to work with the DateTimeInterface.

    Commits
    -------

    f1f9754 [HttpFoundation][Cookie] Cookie DateTimeInterface fix