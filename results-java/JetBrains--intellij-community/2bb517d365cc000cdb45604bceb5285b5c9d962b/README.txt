commit 2bb517d365cc000cdb45604bceb5285b5c9d962b
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Jul 13 20:24:26 2015 +0300

    Several improvements in Python PSI API related to imports

    * Pull PyFileImpl#findExportedName up to PyFile interface
    * PyFromImportStatement#resolveImportSource returns PsiFileSystemItem