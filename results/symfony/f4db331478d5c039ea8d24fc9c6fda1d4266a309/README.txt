commit f4db331478d5c039ea8d24fc9c6fda1d4266a309
Merge: fd65bcc 50ca944
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Feb 20 09:20:53 2017 -0800

    minor #21676 [Serializer] Reduce complexity of NameConverter (gadelat)

    This PR was merged into the 3.3-dev branch.

    Discussion
    ----------

    [Serializer] Reduce complexity of NameConverter

    Cleaner and faster implementation of camelcase normalization.
    Speed improvement is particularly visible in long string input.

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Commits
    -------

    50ca944278 [Serializer] Reduce complexity of NameConverter