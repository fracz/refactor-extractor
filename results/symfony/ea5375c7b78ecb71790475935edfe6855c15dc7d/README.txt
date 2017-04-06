commit ea5375c7b78ecb71790475935edfe6855c15dc7d
Author: Jules Pietri <heah@heahprod.com>
Date:   Tue Mar 15 17:34:45 2016 +0100

    [Form] refactor CheckboxListMapper and RadioListMapper

    fixes #14712 and #17789.

    `ChoiceType` now always use `ChoiceToValueTransformer` or
    `ChoicesToValuesTransformer` depending on `multiple` option.
    Hence `CheckboxListMapper` and `RadioListMapper` don’t handle
    the transformation anymore.
    Fixes pre selection of choice with model values such as `null`,
    `false` or empty string.