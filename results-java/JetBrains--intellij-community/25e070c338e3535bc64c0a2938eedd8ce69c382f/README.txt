commit 25e070c338e3535bc64c0a2938eedd8ce69c382f
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Oct 2 20:58:05 2014 +0400

    All-around check that operator precedence is not broken during inlining

    * Add missing priorities for operators: XOR, boolean NOT and floordiv.
    * Always surround ternary conditional operator in braces if it's going to be inlined
    in another conditional expression for readability sake (though it does not break
    semantics if we inline in the else branch).