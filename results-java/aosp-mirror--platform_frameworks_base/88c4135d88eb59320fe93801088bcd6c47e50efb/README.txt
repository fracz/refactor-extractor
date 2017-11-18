commit 88c4135d88eb59320fe93801088bcd6c47e50efb
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Apr 7 15:18:58 2016 -0700

    Fix issue #26879170: Adjust doze maintenance windows...

    ...based on network availability

    There is a new light maintenance state "waiting for network"
    that we go in to after idle if the network is not currently
    available.  We will stay in this state the same duration as idle,
    so if we are continually without network access this effectively
    doubles the light doze idle durations.

    Get rid of some wrongly copy/pasted code to allow doze light
    to still work even if we don't have an SMD.  It doesn't need
    one.

    Also a bunch of improvements to the shell commands to make it
    easier to test / debug.

    Change-Id: Iad024840661479dbfd54b5b3db6ab96fefe59bc0