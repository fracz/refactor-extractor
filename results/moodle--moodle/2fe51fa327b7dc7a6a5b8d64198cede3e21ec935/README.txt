commit 2fe51fa327b7dc7a6a5b8d64198cede3e21ec935
Author: Dan Poltawski <dan@moodle.com>
Date:   Fri Jun 2 07:36:40 2017 +0100

    MDL-58646 lib: import latest sabberworm/PHP-CSS-Parser

    This version includes a fix[1] contributed by Frédéric Massart investigating
    our compilation time issues, which significantly improves the speed of
    processing font awesome.

    Thanks, Fred!

    [1] https://github.com/sabberworm/PHP-CSS-Parser/pull/120