commit 0b4daca9ba54b7252ea8c159218391380eb00c8a
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Apr 27 09:47:32 2015 -0700

    Implement user-settable power save whitelist.

    The whitelist is now maintained by DeviceIdleController,
    which is moving out into its own independent system service.
    Network stats now queries it for the whitelist, instead of
    collecting that itself.

    Also did a few improvements in alarm manager -- made the
    code for moving alarms out of the pending list more robust,
    and fixed the debug output to always print the contents of
    the pending list even if we aren't in a pending state.  (That
    would have helped me identify the problem much earlier.)

    Change-Id: I0f7119d4c553c3af4d77b2f71246fa6e2c13c561