commit dd6515b6943ef0fc5fba1c915d3b978f86f5e425
Author: leaf corcoran <leafot@gmail.com>
Date:   Fri Mar 18 01:51:31 2011 -0700

    made some improvements to math expression parsing

    Can bunch operators and numbers together now. Whitespace
    is more intelligently handled depending on context of
    expression.

    See `tests/inputs/math.less` for a detailed overview.