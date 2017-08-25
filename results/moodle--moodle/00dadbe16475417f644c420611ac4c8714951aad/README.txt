commit 00dadbe16475417f644c420611ac4c8714951aad
Author: Petr Skoda <skodak@moodle.org>
Date:   Tue Mar 2 11:51:36 2010 +0000

    MDL-21742 fixed serious regressions - require_login(); could not be called after setting up themes (we need this in several places, this will be improved later) + it is too late to set up default local in require_login() because some scripts may not even call this - such as CLI