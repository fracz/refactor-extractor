commit c3756eb800d90780724b2659404a4fd8e10f55d0
Merge: c9e049b 3158c41
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Apr 21 16:28:51 2013 +0200

    merged branch webfactory/issue-7230/picks/cache-file-utils (PR #7753)

    This PR was squashed before being merged into the master branch (closes #7753).

    Discussion
    ----------

    Mitigate dependency upon ConfigCache

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes (refactoring)
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        | todo, issue is symfony/symfony-docs#2531

    Some clients use ConfigCache only for its ability to atomically write into a file.

    The PR moves that code into the Filesystem component.

    It's a pick off #7230.

    __To-Do:__
    - [ ] Update docs for the Filesystem component

    Commits
    -------

    3158c41 Mitigate dependency upon ConfigCache