commit 90d74a3645ab0b720023f0b874184df63dc4e32d
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Mar 12 12:30:17 2013 -0500

    [#4009] improvement to logic flow

    - Pull driver name once to a local variable
    - use local variable for comparisons
    - move elseif onto same line as closing brace of previous block (per CS)