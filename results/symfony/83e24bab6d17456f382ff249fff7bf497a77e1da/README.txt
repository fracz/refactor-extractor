commit 83e24bab6d17456f382ff249fff7bf497a77e1da
Merge: 2e93849 73b812e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jun 6 16:41:31 2016 +0200

    minor #18966 [console] Improve table rendering performance (aik099)

    This PR was submitted for the master branch but it was merged into the 2.7 branch instead (closes #18966).

    Discussion
    ----------

    [console] Improve table rendering performance

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.8
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #18957
    | License       | MIT
    | Doc PR        | -

    With these improvements on my application, that is rendering table with 128 rows and 5 columns I've cut off total application runtime from **0.39s** to **0.26s**.

    Commits
    -------

    73b812e Make one call to "OutputInterface::write" method per table row