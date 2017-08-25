commit e884f63a0ca20a58b00c41940ea82b8545503e98
Author: Petr Skoda <skodak@moodle.org>
Date:   Tue Aug 24 08:50:53 2010 +0000

    MDL-23911 login as session can be only terminated by logout, this should improve security on pages where user may enter JS that only he/she can see - such as the /my/index.php