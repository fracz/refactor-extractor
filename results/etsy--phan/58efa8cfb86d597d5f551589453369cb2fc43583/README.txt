commit 58efa8cfb86d597d5f551589453369cb2fc43583
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Fri Jun 2 09:03:29 2017 -0700

    WIP - Support analyzing `if ($x !== false)` (need tests)

    Add heuristic to avoid PhanTypeMismatchProperty with true and false.
    Avoid emitting issues in the following case:

    ```php
    $this->prop = true;
    $this->prop = false;
    ```

    But continue inferring `int|false` for this case:

    ```php
    $this->prop = 2;
    $this->prop = false;
    ```

    Fix exception, improve inferences for !is_type($var)

    Catch edge cases and improve code coverage for $$var, ${42}, etc.

    Clean up code for ast version <= 20, add union type visitor for ($x op= $y)
    - Remove references to \ast\flags\ASSIGN_, not in version > 20
    - Remove acceptModifierFlagVisitor, can have 'private final'
    - Clean up falseExpr and trueExpr, those were removed from php-ast
      in 1ca15bdf0d3e1adae925b835339cea71400bc24d of the php-ast repo

    Fix remaining problems
    wip for is_a(x, 'ClassName') and is_a(x, ClassName::class)