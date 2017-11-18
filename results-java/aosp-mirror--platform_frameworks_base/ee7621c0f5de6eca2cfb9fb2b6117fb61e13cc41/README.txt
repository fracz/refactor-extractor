commit ee7621c0f5de6eca2cfb9fb2b6117fb61e13cc41
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Aug 13 16:42:18 2012 -0700

    Modify how the background process LRU list is handled.

    A long time ago, we had a concept of an "empty" process -- this was
    a process that didn't have any interesting components in it, which
    would be placed below everything else in the LRU list.

    Empty processes didn't work out well, because you could get into
    bad situations where you have filled your LRU list with things that
    have hidden activities, pushing empty processes to the bottom and
    being immediately killed as soon as they go into the list.  So this
    was removed.

    This change brings the concept back, but in a slightly different
    form, to address a more specific problem: for people who are switching
    between N different applications, we would like to try to keep those
    activities available in RAM in a consistent manner.  Currently the
    previous activities would be killed often quickly and suprisingly,
    even on devices with lots of RAM.  This is for two reasons:

    (1) As you sit in one application, other things going on in the
    background will go to the top of the LRU list, pushing down the
    previous apps you have visited, even though you aren't aware at all
    of these other things executing.
    (2) There is a hard limit on the number of background processes
    (currently 16) after which they are killed regardless of the amount
    of available RAM.  This is desireable because if there is lots of
    RAM we can end up with tons and tons of processes sitting around,
    not really serving any purpose, but using up resources.

    To improve the situation, we have again a concept of "empty" processes
    but now it means one with no activities.  Processes that aren't empty
    but in the background list are called hidden.  We maintain these as
    two parallel lists, each getting half of the process limit: so with
    a 16 process limit, you can have at most 8 empty and 8 hidden processes.

    This allows us to consistently keep up to 8 recent applications around
    for fast app switching; we will also keep around 8 other processes to
    make it more efficient for background work to execute again if it needs
    to.

    Change-Id: Iee06e45efc20787da6a1e50020e5421c28204bd7