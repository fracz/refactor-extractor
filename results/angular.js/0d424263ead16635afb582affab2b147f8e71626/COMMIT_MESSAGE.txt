commit 0d424263ead16635afb582affab2b147f8e71626
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Sun Nov 23 23:08:33 2014 +0100

    refactor($parse): new and more performant $parse

    Change the way parse works from the old mechanism to a multiple stages
    parsing and code generation. The new parse is a four stages parsing
    * Lexer
    * AST building
    * AST processing
    * Cacheing, one-time binding and `$watch` optimizations

    The Lexer phase remains unchanged.

    AST building phase follows Mozilla Parse API [1] and generates an AST that
    is compatible. The only exception was needed for `filters` as JavaScript
    does not support filters, in this case, a filter is transformed into a
    `CallExpression` that has an extra property named `filter` with the value
    of `true`.

    The AST processing phase transforms the AST into a function that can be
    executed to evaluate the expression. The logic for expressions remains
    unchanged. The AST processing phase works in two different ways depending
    if csp is enabled or disabled. If csp is enabled, the processing phase
    returns pre-generated function that interpret specific parts of the AST.
    When csp is disabled, then the entire expression is compiled into a single
    function that is later evaluated using `Function`. In both cases, the
    returning function has the properties `constant`, `literal` and `inputs`
    as in the previous implementation. These are used in the next phase to
    perform different optimizations.

    The cacheing, one-time binding and `$watch` optimizations phase remains
    mostly unchanged.

    [1] https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Parser_API