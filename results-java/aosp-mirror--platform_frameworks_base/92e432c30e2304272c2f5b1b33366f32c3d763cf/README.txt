commit 92e432c30e2304272c2f5b1b33366f32c3d763cf
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Tue Dec 15 19:17:09 2015 -0800

    Refactor and improve window layering.

    This CL moves layer calculation into a separate class, so the whole
    logic can be encapsulated. It also extracts special cases from the
    general loop and instead applies them at the end. This simplifies the
    logic in the main algorithm and makes it clearer what needs to happen
    for special cases.

    Bug: 26144888

    Change-Id: I87347bf0198bd0d3cd09e4231b4652ab979f2456