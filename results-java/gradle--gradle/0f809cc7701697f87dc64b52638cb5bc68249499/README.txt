commit 0f809cc7701697f87dc64b52638cb5bc68249499
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Oct 8 18:13:09 2014 +0200

    classloader caching - improved classpath hashing

    Improved dir snapshots, prevents symlink recursion issues. Added more coverage.

    +review REVIEW-5219