commit c4271f1fa429e920f5a9629804355c10756a589f
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sun Aug 30 21:26:50 2015 +0200

    [FIX] fix DatabaseHelper

    the DatbaseHelper did not verfiy the return value of the simplexml
    creation function. This has been fixed by checking for XML open and
    parse errors.

    Additionally the code-flow has been improved in detectDbSettings() incl.
    removal of redundant checks und superfluous if usage.

    DatabaseHelper::getMysqlVariableValue() failed with a non-catchable
    fatal error when asked for a non-existent mysql server variable.

    This has been fixed by checking the postcondition and in case the query
    operation failed a RuntimeException will be thrown.

    Additionally that method didn't allow to query any valid variable
    identifier and the method was undocumented regarding quoting. Because of
    it's deficiencies in it's interface and contract, an explicit variant named
    DatabaseHelper::getMysqlVariable() has been added.

    Testcases have been updated regarding those two Mysql variables related
    methods. The testcases are showing how the methods are supposed to work
    and how those two similar named methods differ.

    Conflicts:
            src/N98/Util/Console/Helper/DatabaseHelper.php