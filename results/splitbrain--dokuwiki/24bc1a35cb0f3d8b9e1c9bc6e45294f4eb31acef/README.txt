commit 24bc1a35cb0f3d8b9e1c9bc6e45294f4eb31acef
Author: matthiasgrimm <matthiasgrimm@users.sourceforge.net>
Date:   Mon Jan 30 20:27:50 2006 +0100

    MySQL canDo() patch

    This patch adds a canDo() function to the MySQL backend to
    give higher program levels the opportunity to find out what
    functions the MySQL backend provides.

    Furthermore the option encryptPass was renamed to
    forwardClearPass because the old name was misleading and not
    clear.

    Last but not least the mysql.conf.php was reorganized to make
    clear which SQL statements enable which functions.

    darcs-hash:20060130192750-7ef76-2ba9388ea56b17e4f26feda74a66b7d9b8da7333.gz