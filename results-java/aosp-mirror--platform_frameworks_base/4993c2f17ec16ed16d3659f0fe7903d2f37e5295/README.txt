commit 4993c2f17ec16ed16d3659f0fe7903d2f37e5295
Author: Winson <winsonc@google.com>
Date:   Thu Nov 19 10:06:06 2015 -0800

    Initial changes to stack layout to support paging and nonsquare thumbs.

    - Moving to a couple piecewise curves to define the various overview
      layout states.  Added a new state for focus (to be used in follow up
      CL) to control paging of overview from the nav bar button.  This
      allows us to control the visible range of items on the curve, and
      to better fit other UI controls around the stack.
    - Removed the scaling of the tasks in the stack
    - Also refactoring parametric curve to just use the system Path

    Change-Id: I4108da77986d86896576e36fa8f31189d6fcb6f3