commit 66e7945f29a653ed79bc13cf43b997fed3b23476
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Tue Feb 21 20:44:56 2012 +0400

    minor JDR refactoring

    * function descriptor cache is no longer needed in JDR
    * fixed incorrect assertion
    * removed unused parameter
    * better exception message

    Related to http://ea.jetbrains.com/browser/ea_problems/33872