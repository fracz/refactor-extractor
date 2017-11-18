commit 70236bf4f10b485c20fd7aab80c9309a8b8f5cbf
Author: Robert Greenwalt <rgreenwalt@google.com>
Date:   Fri Aug 8 14:19:58 2014 -0700

    Remove extraneous clearing of inet condition

    This was old code I missed in previous inet condition refactor
    and caused us to show "not connect" icon any time we connected
    to a secondary network (mms/supl/etc).

    bug:16896743
    Change-Id: I0fa62e09bb0b7c0ee0864bb1f95967eac5f60d3e