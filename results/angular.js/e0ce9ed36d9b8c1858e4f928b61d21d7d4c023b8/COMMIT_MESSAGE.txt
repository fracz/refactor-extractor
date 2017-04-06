commit e0ce9ed36d9b8c1858e4f928b61d21d7d4c023b8
Author: RoyLING <royling0024@gmail.com>
Date:   Sun Jan 5 15:51:04 2014 +0800

    refactor(filterFilter): simplify code by a ternary op instead of if-else

    - use only one IIFE and a ternary op in it, instead of invoking separate IIFEs in if-else
    (this also completely fixed the same issue closed by PR #3597)
    - also add a spec to verify usage of '$' property in expression object (e.g. `{$: 'a'}`)

    Closes #5637