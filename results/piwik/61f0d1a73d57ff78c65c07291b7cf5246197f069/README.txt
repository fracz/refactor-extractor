commit 61f0d1a73d57ff78c65c07291b7cf5246197f069
Author: robocoder <anthon.pang@gmail.com>
Date:   Sun Jun 6 03:26:30 2010 +0000

    refs #1335, refs #1368 - check in Jason's Piwik_Db_Adapter_Pdo_Mssql class (excluding the schema and query methods being refactored elsewhere; edited to conform to coding style guidelines; note: a lot of the logic in getConnection() looks like it should be moved upstream to ZF (2.0 CTP1 related?) especially if CTP2 introduces breaking changes to the DSN

    git-svn-id: http://dev.piwik.org/svn/trunk@2278 59fd770c-687e-43c8-a1e3-f5a4ff64c105