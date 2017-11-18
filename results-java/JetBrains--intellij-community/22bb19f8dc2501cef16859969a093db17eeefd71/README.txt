commit 22bb19f8dc2501cef16859969a093db17eeefd71
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Mon Feb 18 15:13:30 2013 +0400

    Subversion: 1) svn tests setup - checkout from svn before change list manager is initialized
    2) new methods for native svn execution, to re-query or re-commit if database was locked
    + add tests refactored to use new methods