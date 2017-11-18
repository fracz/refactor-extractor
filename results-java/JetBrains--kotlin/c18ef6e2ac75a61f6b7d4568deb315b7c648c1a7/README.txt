commit c18ef6e2ac75a61f6b7d4568deb315b7c648c1a7
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jun 3 15:04:48 2015 +0300

    Increase buffer size in preloader from 8K to 512K

    Doesn't seem to improve performance, but cuts down lots of IO file read counts