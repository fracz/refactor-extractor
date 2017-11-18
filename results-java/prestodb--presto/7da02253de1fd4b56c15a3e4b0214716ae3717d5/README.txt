commit 7da02253de1fd4b56c15a3e4b0214716ae3717d5
Author: Grzegorz Kokosiński <Grzegorz.Kokosinski@Teradata.com>
Date:   Wed Aug 24 08:00:21 2016 +0200

    Add test to check if correlation is resolved correctly

     In order to that some modification to logical plan assertions had to be
     applied:
     - Adding CorrelationMatcher
     - refactoring SymbolAliases to ExpressionAliases
     - extending ExpressionVerifier to support more expressions