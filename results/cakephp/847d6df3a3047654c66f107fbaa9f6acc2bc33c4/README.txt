commit 847d6df3a3047654c66f107fbaa9f6acc2bc33c4
Author: brego <brego@3807eeeb-6ff5-0310-8944-8be069107fe0>
Date:   Sun Jun 19 23:30:36 2005 +0000

    PHP5 bugs:
    - "public" cannot be defined as constant (PHP5 keyword)
    - Cannot re-assign $this in libs/template.php on line 217...

    Other:
    - Started on refactoring as described in #27 - but this feels like fooled aproach. We need to modularize HTML and AJAX stuff to be included only on-call.



    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@256 3807eeeb-6ff5-0310-8944-8be069107fe0