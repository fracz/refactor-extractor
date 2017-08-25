commit 089f1f065e312c108fb19306c1c89e064d417669
Author: Eloy Lafuente <stronk7@moodle.org>
Date:   Tue Nov 17 15:51:27 2009 +0000

    MDL-20846 creating users on restore - part1 - mark password as 'restored'
    so login will detect that for resetting password. Also some minor improvements
    into user/auth detection. Merged from 19_STABLE