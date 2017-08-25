commit 9baf267016c5d7388e2ce78531c9cd5b7c29ee3d
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Apr 1 13:09:24 2010 +0000

    tablelib: MDL-22011 refactor flexible_table::get_sql_sort into several smaller methods.
    Also, change assignemnt to use a separate static method, rather than overloading get_sql_sort.