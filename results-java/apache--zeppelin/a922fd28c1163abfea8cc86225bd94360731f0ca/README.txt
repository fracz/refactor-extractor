commit a922fd28c1163abfea8cc86225bd94360731f0ca
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Wed Aug 3 17:07:54 2016 +0900

    Small refactoring of Python interpreter

    ### What is this PR for?
    Small refactoring of Python interpreter, that is what it is.

    ### What type of PR is it?
    Refactoring

    ### Todos
    * [x] refactor `help()`
    * [x] impl `maxResult` fetch from JVM

    ### How should this be tested?
    `cd python && mvn -Dpython.test.exclude='' test ` pass (given that `pip install pandasql` and `pip install py4j`)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Alexander Bezzubov <bzz@apache.org>

    Closes #1275 from bzz/python/refactoring and squashes the following commits:

    15a35c8 [Alexander Bezzubov] Make .help() method a single string literal
    e800fd7 [Alexander Bezzubov] Make Python fetch maxResults from JVM