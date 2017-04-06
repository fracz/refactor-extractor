commit 85fd8754ec18823b29955620653cf79a38d4f290
Merge: 02f66e2 2bb5f45
Author: Kévin Dunglas <dunglas@gmail.com>
Date:   Tue Jan 19 09:01:47 2016 +0100

    minor #17286 [Serializer] Refactor serializer normalize method (jvasseur)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Serializer] Refactor serializer normalize method

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    The serializer normalize method contains some dead or duplicate code. This PR refactor the method to make it simpler.

    I tried to keep the same Exceptions in the same situations but they could probably be simplified too. (The last case only append if you pass a ressource to the serializer.)

    Commits
    -------

    2bb5f45 Refactor serializer normalize method