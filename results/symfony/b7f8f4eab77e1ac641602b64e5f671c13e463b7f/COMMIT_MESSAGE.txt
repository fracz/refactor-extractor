commit b7f8f4eab77e1ac641602b64e5f671c13e463b7f
Merge: f3ad406 c77fdcb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Nov 2 02:10:02 2014 +0100

    bug #12370 [Yaml] improve error message for multiple documents (xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Yaml] improve error message for multiple documents

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

    The YAML parser doesn't support multiple documents. This pull requests
    improves the error message when the parser detects multiple YAML
    documents.

    see also #11840

    Commits
    -------

    c77fdcb improve error message for multiple documents