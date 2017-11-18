commit 1b4447f728eab763beb2da28d3cf64574729c556
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Jul 20 14:49:58 2015 -0700

    Fix issue #22564918: Request is inactive

    Add Request.isActive() API.  Also improve documentation to tell
    people what things cause it to become inactive.  And fix a race
    where we were modifying the active list from outside the main
    thread without locking it.

    Change-Id: I9248e014126cb121612edbe595108ace753456e2