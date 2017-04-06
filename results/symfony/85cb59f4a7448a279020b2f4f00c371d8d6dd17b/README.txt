commit 85cb59f4a7448a279020b2f4f00c371d8d6dd17b
Merge: 287db73 e448fad
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jun 19 17:00:36 2015 +0200

    bug #15036 [VarDumper] Fix dump output for better readability (nicolas-grekas)

    This PR was merged into the 2.6 branch.

    Discussion
    ----------

    [VarDumper] Fix dump output for better readability

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This PR is a follow up of the feedback provided by @bobthecow while proposing var-dumper on bobthecow/psysh#184

    It tweaks the output of dumps to (hopefully) make it more readable. The updated test cases in the attached patch should be enough to understand the differences, which are mainly:
    - change the displaying of control chars (`\x00` instead of `@`)
    - show the `\n` character at end of lines (and display `"foo\n"` on a single line)
    - label resources as `foo resource` instead of `:foo` and remove their empty brackets (`{}`) when they have no meta-data
    - add a missing hook when reaching end of values

    Commits
    -------

    e448fad [VarDumper] Fix dump output for better readability