commit a8c10ac6e516ffea1c5363dc64b1110fa1d32cb9
Merge: 4aca703 ea5375c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Apr 28 11:51:12 2016 +0200

    bug #18180 [Form] fixed BC break with pre selection of choices with `ChoiceType` and its children (HeahDude)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Form] fixed BC break with pre selection of choices with `ChoiceType` and its children

    | Q             | A
    | ------------- | ---
    | Branch        | 2.7+
    | Bugfix?  | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #18173, #14712, #17789
    | License       | MIT
    | Doc PR        | -

    - f7eea72 reverts BC break introduced in #17760
    - 58e8ed0 fixes pre selection of choice with model values such as `false`, `null` or empty string without BC break.

    `ChoiceType` now always use `ChoiceToValueTransformer` or `ChoicesToValuesTransformer` depending on `multiple` option.
    Hence `CheckboxListMapper` and `RadioListMapper` don't handle the transformation anymore.

    Commits
    -------

    ea5375c [Form] refactor CheckboxListMapper and RadioListMapper
    71841c7 Revert "[Form] refactor `RadioListMapper::mapDataToForm()`"