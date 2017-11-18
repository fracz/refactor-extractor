commit c0bd747b0605af251ff136277f14220a5a4c9818
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Oct 9 14:00:30 2012 -0700

    Further work on issue #7307399: Framework needs a new pre-user-shutdown...

    ...phase & callback API

    I realized there were a few things wrong with what was there.  The new
    ACTION_USER_STARTING was not being sent for the first user at boot, and
    there was an existing problem where ACTION_USER_STARTED was sent every
    time there was a user switch.

    Also improved some debug output of broadcasts to make it easier to see
    what is going on in this stuff, and better reporting of why a service
    couldn't be started.

    Change-Id: Id8a536defbbad1f73d94a37d13762436b822fbe3