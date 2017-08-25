commit 4549cf519e838a2dd2828453c4157231bccd3287
Author: Vincent Petry <PVince81@yahoo.fr>
Date:   Thu Aug 29 20:59:45 2013 +0200

    Added missing "files" JS to files_trashbin module

    The recent refactoring for the breadcrumb resizing relies on the "Files"
    object which is in the "files" Javascript file.

    This fix includes it here as well.