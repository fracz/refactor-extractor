commit 66d7730903a0163711e3d037c2350d6a13368004
Author: Craig Mautner <cmautner@google.com>
Date:   Wed Apr 10 15:33:26 2013 -0700

    Debug logging improvement.

    Previously a change to a surface would be logged with the old value
    and you had to scroll through the logs to see what the new value
    was. This change reflects the change to the surface immediately.

    Change-Id: I2a6566466287922d08f4ce2329c61aa46d692ee1