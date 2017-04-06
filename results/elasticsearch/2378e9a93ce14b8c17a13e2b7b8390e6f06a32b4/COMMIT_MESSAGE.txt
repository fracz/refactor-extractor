commit 2378e9a93ce14b8c17a13e2b7b8390e6f06a32b4
Author: Costin Leau <costin.leau@gmail.com>
Date:   Mon Oct 14 23:55:05 2013 +0300

    improve escaping of user paths

    remove usage of if and () blocks as they clashed with user paths containing ()
    postpone quotation of variables to prevent double escaping ("")
    fix #3906

    (cherry picked from commit 1cc095ec32a5b623f88c312f497ac5469887be97)