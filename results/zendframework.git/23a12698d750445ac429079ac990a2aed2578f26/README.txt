commit 23a12698d750445ac429079ac990a2aed2578f26
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Thu Mar 15 23:51:50 2012 -0500

    Zend\Db Features and Refactoring (story #41)
    * Fixed PDO Connection with sqlite
    * Added a merge to the ParameterContainer
    * Delete, Insert, Update now extends AbstractSql, uses shared implementation
    * Created Sql factory, to be used as a dependency in TableGateway
    * renamed "database" to "scheme" in a dozen places
    * Cleaned up unit tests for refactoring