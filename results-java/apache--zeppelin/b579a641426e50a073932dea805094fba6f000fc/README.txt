commit b579a641426e50a073932dea805094fba6f000fc
Author: Sotnichenko Sergey <s.sotnichenko@tinkoff.ru>
Date:   Thu Feb 9 09:06:22 2017 +0300

    [ZEPPELIN-1876] improved comptetion with schema/table/column separati…

    …on + sqlcompleter tests

    ### What is this PR for?
    This PR changes autocompletion behaviour in jdbc interpeter.
    There are some changes:
    * [main change] autocompletion now depends on what are you typing. Now there are four types of competion: schema, table, column and keywords. If you typing new word then autocompetion suggests only keywords and schema names. If you are typing after schema name with point then you get list of tables in that schema. Also if you typing a name after point after a table name you will get a list of column names of this table.
    * autocomption now supports aliases in sql. If you write alias for a table in sql you will get a list of columns for an aliased table if you write down alias and point.
    * autocompletion now load keywords only in low case (otherwise there are so many keywords in a list of autocompletion that it is becomes uncomfortable)

    ### What type of PR is it?
    Improvement

    ### Todos
    * [ ] - sort names in the output of autocompletion
    * [ ] - list only schema names if we are typing a schema name (for example after keywork FROM)
    * [ ] - add description in autocompletion list for schema names - schema, for table names - table and so
    * [ ] - autocompletion must initialize on opening of interpeter (not only after execution of sql)
    * [ ] - update autocompletion schemas only after execute update sql, not after every sql ???
    * [ ] - new option for postgresql interpreter: postgresql.completer.schema.filter. Filter schema names loaded into autocompletion (no more garbage schema names).

    ### What is the Jira issue?
    * Open an issue on Jira https://issues.apache.org/jira/browse/ZEPPELIN/
    * Put link here, and add [ZEPPELIN-*Jira number*] in PR title, eg. [ZEPPELIN-533]

    ### How should this be tested?
    Outline the steps to test the PR here.

    ### Screenshots (if appropriate)
    https://issues.apache.org/jira/secure/attachment/12845228/auto1.JPG
    https://issues.apache.org/jira/secure/attachment/12845229/auto2.JPG
    https://issues.apache.org/jira/secure/attachment/12845230/auto3.JPG

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Yes

    Author: Sotnichenko Sergey <s.sotnichenko@tinkoff.ru>

    Closes #1886 from sotnich/jdbc-1876 and squashes the following commits:

    2db7ed8 [Sotnichenko Sergey] [ZEPPELIN-1876] add support for databases with only catalogs without schemas (like mysql)
    a048ef2 [Sotnichenko Sergey] [ZEPPELIN-1876] add support for databases with only catalogs without schemas (like mysql)
    f4b03df [Sotnichenko Sergey] [ZEPPELIN-1876] add support for databases with only catalogs without schemas (like mysql)
    675c629 [Sotnichenko Sergey] [ZEPPELIN-1876] Adding licence header
    9fac1d0 [Sotnichenko Sergey] [ZEPPELIN-1876] SQLException instead of Throwable
    895c35a [Sotnichenko Sergey] [ZEPPELIN-1876] SQLException instead of Throwable
    7d40166 [Sotnichenko Sergey] [ZEPPELIN-1876] improved comptetion with schema/table/column separation + sqlcompleter tests