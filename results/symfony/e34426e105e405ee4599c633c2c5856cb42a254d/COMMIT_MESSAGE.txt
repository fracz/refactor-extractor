commit e34426e105e405ee4599c633c2c5856cb42a254d
Merge: ac528c7 747ddf6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Aug 16 07:44:38 2016 -0700

    minor #19617 Use try-finally where it possible (Koc)

    This PR was merged into the 3.1 branch.

    Discussion
    ----------

    Use try-finally where it possible

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.1
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Just a minior refactoring for using PHP 5.5 feature.

    Commits
    -------

    747ddf6 Use try-finally where it possible