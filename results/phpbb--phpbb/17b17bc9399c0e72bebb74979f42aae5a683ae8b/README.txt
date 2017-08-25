commit 17b17bc9399c0e72bebb74979f42aae5a683ae8b
Author: Igor Wiedler <igor@wiedler.ch>
Date:   Mon Feb 14 09:15:51 2011 +0100

    [task/refactor-db-testcase] Improve error message of db tests

    If database tests cannot be run the error message is ambigous. This
    commit makes it clearer:

    - whether the supplied dbms is supported by us
    - which dbms are supported by us
    - whether the required PDO extension is loaded

    PHPBB3-10043