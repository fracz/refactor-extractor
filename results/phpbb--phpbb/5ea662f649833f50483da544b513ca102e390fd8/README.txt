commit 5ea662f649833f50483da544b513ca102e390fd8
Author: Joas Schilling <nickvergessen@gmx.de>
Date:   Sat Aug 25 14:34:48 2012 +0200

    [ticket/11014] Restore template vars for next/previous links

    They were dropped while the function was refactored:

    If the block_var_name is a nested block, we will use the last (most inner)
    block as a prefix for the template variables. If the last block name is
    pagination, the prefix is empty. If the rest of the block_var_name is not
    empty, we will modify the last row of that block and add our pagination items.

    PHPBB3-11014