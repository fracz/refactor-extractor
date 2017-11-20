commit 24fe6a7a3e839eaaccffb7901c41385a9305d070
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Fri Jul 25 13:28:40 2014 -0700

    [Java]: refactor some of the pub and sub rollover tests. Added min check in LogScanner to keep it passing capacity when scanning over padding.