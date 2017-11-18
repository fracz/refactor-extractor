commit c5faa142fb25abe20b69eae0c03a8f5c9e4b3165
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Sun May 8 20:56:26 2016 -0700

    KObjectHashMap probeNext improvement

    Motivation:
    KObjectHashMap.probeNext(..) usually involves 2 conditional statements and 2 aritmatic operations. This can be improved to have 0 conditional statements.

    Modifications:
    - Use bit masking to avoid conditional statements

    Result:
    Improved performance for KObjecthashMap.probeNext(..)