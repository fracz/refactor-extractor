commit 9716c35f6d3a68e410a9f05b60ab4b4436aeb706
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Aug 7 16:17:50 2007 +0000

    - reorganized LogStats modules into separate files
    - edited config.php to be able to change to path (simpletest was annoying and wouldn't want to behave properly with my include_path)
    - implemented a simple class for Table partitionning by date (month or day). It generates a table name depeding on a given date. Useful for the archives and maybe later for the logging engine. + wrote tests for this class

    git-svn-id: http://dev.piwik.org/svn/trunk@30 59fd770c-687e-43c8-a1e3-f5a4ff64c105