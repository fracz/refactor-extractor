commit dc6d67510d65393eabf420cc6648e8f31acb6586
Author: Sam Brannen <sam@sambrannen.com>
Date:   Tue Mar 18 23:53:43 2014 +0100

    Improve configurability of EmbeddedDatabaseBuilder

    This commit improves the configurability of EmbeddedDatabaseBuilder by
    exposing the following new configuration methods.

     - setDataSourceFactory(DataSourceFactory)
     - addScripts(String...)
     - setScriptEncoding(String)
     - setSeparator(String)
     - setCommentPrefix(String)
     - setBlockCommentStartDelimiter(String)
     - setBlockCommentEndDelimiter(String)
     - continueOnError(boolean)
     - ignoreFailedDrops(boolean)

    If more fine grained control over the configuration of the embedded
    database is required, users are recommended to use
    EmbeddedDatabaseFactory with a ResourceDatabasePopulator and forego use
    of the builder.

    Issue: SPR-11410