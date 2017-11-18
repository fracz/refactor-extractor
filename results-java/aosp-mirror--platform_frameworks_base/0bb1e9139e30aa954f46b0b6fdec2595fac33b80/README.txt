commit 0bb1e9139e30aa954f46b0b6fdec2595fac33b80
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Aug 16 12:54:24 2011 -0700

    Small improvements to am command.

    The start command can now take a package name or component name
    for easier starting.  New -S option allows you to force stop an
    app before starting it.

    Change-Id: I5c55b34dd794783f0f5f51851dc811b8c1b39b76