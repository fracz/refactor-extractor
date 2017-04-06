commit fb791fdbcb0a89dcf9dc3a2d8133855d44cc1421
Merge: d9b8d0c 44a2861
Author: Christophe Coevoet <stof@notk.org>
Date:   Wed Dec 2 16:35:50 2015 +0100

    bug #16772 Refactoring EntityUserProvider::__construct() to not do work, cause cache warm error (weaverryan)

    This PR was submitted for the 2.8 branch but it was merged into the 2.3 branch instead (closes #16772).

    Discussion
    ----------

    Refactoring EntityUserProvider::__construct() to not do work, cause cache warm error

    | Q             | A
    | ------------- | ---
    | Bug fix?      | "yes"
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | none
    | License       | MIT
    | Doc PR        | n/a

    This fixes a "Database not Found" error when using `doctrine/orm` 2.5 while warming up your cache under certain situations. Basically, if you use the `EntityUserProvider`, then during cache warmup, Twig requires the `security.authorization_checker` which eventually requires this `EntityUserProvider`, which previously caused Doctrine to calculate the metadata for your User class. If no database exists (and you haven't specified the platform), you'll get the error.

    More broadly, this simply tries to do less work in the constructor. It's a "bug fix", only kind of, but as it's completely an internal refactoring, it should be quite safe.

    Thanks!

    Commits
    -------

    44a2861 Refactoring EntityUserProvider::__construct() to not do work, cause cache warm error