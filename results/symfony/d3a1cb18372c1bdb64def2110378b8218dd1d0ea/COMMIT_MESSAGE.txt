commit d3a1cb18372c1bdb64def2110378b8218dd1d0ea
Merge: e894867 799d0e0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 12 11:59:37 2015 +0200

    bug #15251 [DoctrineBridge][Form] Fix IdReader when indexing by primary foreign key (giosh94mhz)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [DoctrineBridge][Form] Fix IdReader when indexing by primary foreign key

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

    Port to Symfony 2.7 of PR #14372 . The `IdReader` class can now resolve association and determine the real id value.

    There is still room for improvement, though. Since I've added a `new IdReader` in the constructor, it is better to declare `IdReader` as `final` just to be safe.

    PS: sorry to keep you waiting, @webmozart . When merging both commit don't forget to add the `@deprecated` annotation, and that `SingleAssociationToIntIdEntity.php` is duplicated.

    Commits
    -------

    799d0e0 [DoctrineBridge][Form] Fix IdReader when indexing by primary foreign key