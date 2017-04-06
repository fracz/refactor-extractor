commit b5d050186028727a930ae5afced596a3ba256b63
Merge: bdb01a6 000bd0d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Aug 31 05:48:56 2014 +0200

    minor #11574 [Security] Made optimization on constant-time algorithm removing modulus operator (yosmanyga)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Security] Made optimization on constant-time algorithm removing modulus operator

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This fix improves the constant-time algorithm used to compare strings, as it removes the `%` operator inside the loop.

    Commits
    -------

    000bd0d Made optimization deprecating modulus operator