commit 2f4417137c03084e2d039caf47e8fd6461683159
Merge: 5ad49c6 049fdfe
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 30 17:38:50 2015 +0200

    feature #15503 UI & CSS improvement to new toolbar (WouterJ)

    This PR was merged into the 2.8 branch.

    Discussion
    ----------

    UI & CSS improvement to new toolbar

    * Created a generic class for right floating toolbar blocks, so it can be reused by other blocks serving the same goal (the Sylius toolbar block for instance)
    * Added a way to group toolbar info pieces, this makes the bigger info blocks easier to read:

      ![sf-toolbar-group](https://cloud.githubusercontent.com/assets/749025/9178378/41987f02-3f97-11e5-971e-37f44a47d56d.png)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Commits
    -------

    049fdfe Add a way to group toolbar info pieces
    23b8a56 Added general sf-toolbar-block-right class