commit eed7a864d8e874292fbfc6fd18c09196036da15e
Merge: 6b0b504 995a033
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Feb 24 17:11:10 2014 +0100

    minor #10317 [YAML] Improve performance of getNextEmbedBlock (alexpott)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [YAML] Improve performance of getNextEmbedBlock

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    By removing unnecessary preg_match and function calls - isCurrentLineEmpty() contains a call to isCurrentLineBlank() - therefore this function is called twice every time this condition is hit. The preg_match appears to legacy handling of blank lines.

    This improves the performance of the Drupal 8 installer.
    ![image](https://f.cloud.github.com/assets/769634/2241426/69effb0c-9cd1-11e3-9145-e4fabd2ec870.png)

    Commits
    -------

    995a033 Improve performance of getNextEmbedBlock by removing unnecessary preg_match and function calls.