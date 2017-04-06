commit d0c3ffa28865d47aa74b03c261e86fe53d95a9fe
Merge: 55d17fa a8a40fc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Aug 31 06:14:43 2014 +0200

    bug #10197 [FrameworkBundle] PhpExtractor bugfix and improvements (mtibben)

    This PR was submitted for the master branch but it was merged into the 2.3 branch instead (closes #10197).

    Discussion
    ----------

    [FrameworkBundle] PhpExtractor bugfix and improvements

    PhpExtractor currently only handles simple strings which match an overly-specific token sequence.

    This change adds support for
    - heredoc / nowdoc
    - inconsistent whitespace when parsing
    - escaped sequences in strings
    - `transChoice`

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

    Commits
    -------

    a8a40fc [FrameworkBundle] PhpExtractor bugfix and improvements