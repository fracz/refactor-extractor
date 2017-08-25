commit 7b262babcd5c862c3068b08da8bba5f48bd5f36a
Author: David M <davidmj@users.sourceforge.net>
Date:   Sun Feb 3 10:19:04 2008 +0000

    Alright, this should give some improved performance :)

    This is the end of random seek access to rows. If you have a compelling reason as to why they should stay, contact me. Else, they are gone forevermore...

    The following API calls are deprecated:
    acm::sql_rowseek() -> no replacement
    $db->sql_fetchfield($field, $rownum = false, $query_id = false) -> $db->sql_fetchfield($field, $query_id = false)

    Initial tests show that phpBB3 over four percent of memory against phpBB3.1 on an empty board. So far so good :)

    Other cool things:
    db2, MS SQL ODBC and MS SQL 2005 all use less memory because they do not need to reference the last executed query to handle random access seeks :)

    P.S.
    The crazy people using SVN: please report any issues with the new way we itterate through caches, I do not want to miss anything :)

    git-svn-id: file:///svn/phpbb/trunk@8372 89ea8834-ac86-4346-8a33-228a782c2dd0