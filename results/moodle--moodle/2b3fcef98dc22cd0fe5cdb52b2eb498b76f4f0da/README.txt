commit 2b3fcef98dc22cd0fe5cdb52b2eb498b76f4f0da
Author: Petr Skoda <skodak@moodle.org>
Date:   Wed Dec 16 21:31:47 2009 +0000

    MDL-21142 moodle_url improvements:
    1/ new __toString()
    2/ strict method parameters checking, this should help discover wrong uses
    3/ new remove_all_params() which prevents problems when using remove_params()