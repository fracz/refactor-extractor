commit ebcaa4053325364c0f9f57b129dd46cc2cdaceb1
Author: intrigus <intrigus@users.noreply.github.com>
Date:   Wed Apr 19 17:02:44 2017 +0200

    Improve Performance, don't calculate known values (#4630)

    This improves performance a little bit by not calculating known values.

    In my case it saved about 200ms.