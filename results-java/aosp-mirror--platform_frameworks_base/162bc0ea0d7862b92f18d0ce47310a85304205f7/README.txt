commit 162bc0ea0d7862b92f18d0ce47310a85304205f7
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Apr 9 14:06:16 2012 -0700

    Some small tweaks to improve memory management.

    We now allow processes that currently have stopping activities to
    be managed as if they were done stopping, so that memory trimming
    can be done before the process goes to the background.  Hopefully
    this will reduce cases where the processes goes to the background
    and immediately gets killed, but wouldn't have had to be killed if
    it had a chance to trim its memory.

    Also change window memory trimming to always do the aggressive
    trimming when memory is critical, even if not on a low-end device.

    And tweak web view trimming to not trim for foreground UI events.

    Change-Id: I241b3152b52d09757bd14a202477cf69c9b78786