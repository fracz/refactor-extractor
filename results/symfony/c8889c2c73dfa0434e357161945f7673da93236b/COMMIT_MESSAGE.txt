commit c8889c2c73dfa0434e357161945f7673da93236b
Merge: 0d32445 fb686d8
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Apr 12 12:34:18 2013 +0200

    merged branch pvolok/fix-7274 (PR #7641)

    This PR was merged into the 2.1 branch.

    Discussion
    ----------

    [2.2][Yaml] Fixed resolving blank values

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #7274
    | License       | MIT
    | Doc PR        | -

    Also, Seldaek suggested to rename the $notEOF variable.

    Commits
    -------

    fb686d8 [Yaml] improved boolean naming ($notEOF -> !$EOF)
    047212a [Yaml] fixed handling an empty value