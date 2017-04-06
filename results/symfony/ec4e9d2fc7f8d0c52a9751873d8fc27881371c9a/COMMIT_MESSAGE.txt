commit ec4e9d2fc7f8d0c52a9751873d8fc27881371c9a
Merge: 36948bb 45cfb44
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 25 10:28:01 2015 +0100

    minor #14028 [Security] [Core] String utils refactor (sarciszewski, ircmaxell)

    This PR was submitted for the 2.7 branch but it was merged into the 2.3 branch instead (closes #14028).

    Discussion
    ----------

    [Security] [Core] String utils refactor

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    This supersedes #13984 (it includes it, but also includes additional refactoring).

    Since length information is leaked in any case, preventing unnecessary duplication of secrets is important. Since casting will *always* make a copy, we only cast if absolutely necessary. Additionally, appending will create a new copy of the secret, so we avoid doing that, but instead only iterate over the minimum of the two strings.

    Commits
    -------

    45cfb44 Change behavior to mirror hash_equals() returning early if there is a length mismatch
    8269589 CS fixing
    bdea4ba Prevent modifying secrets as much as possible
    76b36d3 Update StringUtils.php
    7221efc Whitespace
    56ed71c Update StringUtils.php