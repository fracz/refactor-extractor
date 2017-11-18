commit 945c9c9f8f65556dd93218df724be25c634f6db3
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Mar 30 14:55:00 2016 -0700

    Fix issue #27920133: Need to ensure activity starts in idle maintenance

    We now hold a wake lock while going in to idle maintenance, since
    we really want to make sure everyone has a chance to respond.  And
    since we are doing that, we can move to just using a delayed message
    to make sure we don't leave maintenance until the time expires, getting
    rid of the separate timeout alarm.

    Also improve the initial transition to light idle mode so that if
    we currently have work going on, we will wait for up to 15 minutes
    more before actually going idle and forcing it to stop.

    Change-Id: I6045da57ab4165f80a651126e99371c029ced23d