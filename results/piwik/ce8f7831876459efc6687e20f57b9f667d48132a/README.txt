commit ce8f7831876459efc6687e20f57b9f667d48132a
Author: robocoder <anthon.pang@gmail.com>
Date:   Sun Jun 6 19:42:20 2010 +0000

    refs #1368 - more refactoring and phpdocs

     * The abstraction of the DDL is more or less complete.  I still have to implement isAvailable() in core/Db/Schema/Myisam.php.
     * The default schema will be MyISAM.  The update script will add:  schema=Myisam to the ![database] section
     * The schema object is lazy loaded.



    git-svn-id: http://dev.piwik.org/svn/trunk@2281 59fd770c-687e-43c8-a1e3-f5a4ff64c105