commit a679f88f6482a3df0f652f3764518e08e7f87a2c
Author: Daniel Frett <daniel.frett@ccci.org>
Date:   Tue Jan 26 14:28:36 2016 -0500

    provide a knob to disable tgt locking

    Disabling locking improves general performance but may cause deadlocks based on db transaction isolation