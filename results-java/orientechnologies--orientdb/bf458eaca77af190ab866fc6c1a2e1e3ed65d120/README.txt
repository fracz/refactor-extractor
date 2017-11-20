commit bf458eaca77af190ab866fc6c1a2e1e3ed65d120
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Sat Apr 28 16:09:21 2012 +0000

    Huge refactoring to Traverse command:
    - New OTraverse class to be executed without parsing with a fluent interface
    - Changed from stack to iteration model using OCommandProcess classes: less memory and the ability to use context variable in SELECT
    - New OCommandPredicate interface to handle also Non SQL predicates
    - Cleaned OCommandExecutor.parse(): now doesn't take a request text anymore
    - Moved traverse in own package under command