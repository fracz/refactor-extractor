commit 84cc05cacd2786261b01d3be2364944c90765801
Author: Deepanshu Gupta <deepanshu@google.com>
Date:   Tue Aug 12 20:40:42 2014 -0700

    Add KitKat wifi and battery icons.

    This adds wifi and battery icons for Gingerbread and KitKat. This also
    improves the icon resolution code by extracting it out in its own class.
    The resources are now organized such that each API level resource
    directory is used as a backup for all API levels lower than itself.

    Change-Id: I937c83638adcc9fa8cd407e0a3023c3abe95530d