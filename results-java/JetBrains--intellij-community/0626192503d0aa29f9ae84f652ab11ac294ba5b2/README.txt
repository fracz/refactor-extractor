commit 0626192503d0aa29f9ae84f652ab11ac294ba5b2
Author: Anna Kozlova <anna.kozlova@jetbrains.com>
Date:   Thu Oct 23 16:40:50 2014 +0200

    inplace local rename: provide old/new names in events (IDEA-131802)

    as local refactoring doesn't revert template changes before refactoring started local variable name was already changed. if refactoring was ended with invalid name 'after' event got old name because it was already reverted