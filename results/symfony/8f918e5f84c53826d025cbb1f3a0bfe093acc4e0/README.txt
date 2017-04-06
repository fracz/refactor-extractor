commit 8f918e5f84c53826d025cbb1f3a0bfe093acc4e0
Author: Jules Pietri <heah@heahprod.com>
Date:   Fri Feb 19 15:39:54 2016 +0100

    [Form] refactor `RadioListMapper::mapDataToForm()`

    This fixes "false" choice pre selection when `ChoiceType`
    is `expanded` and not `multiple`