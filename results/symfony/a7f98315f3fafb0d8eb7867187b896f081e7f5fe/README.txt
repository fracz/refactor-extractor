commit a7f98315f3fafb0d8eb7867187b896f081e7f5fe
Merge: 40e5f78 8f918e5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Feb 26 06:04:56 2016 +0100

    bug #17760 [2.7] [Form] fix choice value "false" in ChoiceType (HeahDude)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [2.7] [Form] fix choice value "false" in ChoiceType

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #17292, #14712, #17789
    | License       | MIT
    | Doc PR        | -

    - [x] Add tests for choices with `boolean` and `null` values, and with a placeholder
    - [x] Fix FQCN in 2.8 tests, see #17759
    - [x] Remove `choices_as_values` in 3.0 tests, see #17886

    Commits
    -------

    8f918e5 [Form] refactor `RadioListMapper::mapDataToForm()`
    3eac469 [Form] fix choice value "false" in ChoiceType