commit 3cb21c93883beb49756148debaec5dd6fec6c44e
Author: thepurpleblob <thepurpleblob>
Date:   Fri Aug 6 11:11:05 2004 +0000

    fixed bug that must have crept in during recent refactor. $course not being passed
    to export format object.
    export file extension can now be selected by overriding method in format.php