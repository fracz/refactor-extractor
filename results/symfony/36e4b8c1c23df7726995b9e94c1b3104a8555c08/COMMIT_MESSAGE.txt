commit 36e4b8c1c23df7726995b9e94c1b3104a8555c08
Merge: 1a73b44 2769c9d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Aug 9 09:17:14 2013 +0200

    merged branch larowlan/master (PR #8081)

    This PR was submitted for the master branch but it was merged into the 2.2 branch instead (closes #8081).

    Discussion
    ----------

    Use strstr instead of strpos in ClassLoader (4% perf improvement)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | [
    | License       | MIT
    | Doc PR        |

    Using ClassLoader in Drupal 8, using strstr instead of strpos nets 4% perf improvement.
    XHPROF diff https://dl.dropboxusercontent.com/u/10201421/diff.html

    Commits
    -------

    25d7b90 [ClassLoader] Use strstr instead of strpos