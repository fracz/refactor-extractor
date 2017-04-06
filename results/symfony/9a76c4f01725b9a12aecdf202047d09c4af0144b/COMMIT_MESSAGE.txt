commit 9a76c4f01725b9a12aecdf202047d09c4af0144b
Merge: fb0a0a7 bcbbb28
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jun 13 09:48:34 2013 +0200

    merged branch tiagojsag/console_input_options_fix (PR #8199)

    This PR was submitted for the master branch but it was merged into the 2.2 branch instead (closes #8199).

    Discussion
    ----------

    [Console] Throw exception if value is passed to VALUE_NONE input optin, long syntax

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | yes
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8135
    | License       | MIT

    Input options with InputOption::VALUE_NONE accept values in both short and long syntaxes:
    - When using the long syntax, no exception is thrown;
    - When using short, a "The %s option does not exist" exception is thrown.

    This PR only addresses the long syntax case. The short syntax case would require considerable refactoring of the parse code, which I believe should be discussed.

    I included a test that illustrates the above mentioned problem for the long syntax scenario.

    Commits
    -------

    32ea77f Throw exception if value is passed to VALUE_NONE input, long syntax