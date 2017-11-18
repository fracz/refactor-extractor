commit d7dd89095ff2041f0793317c4ee8e8be49388148
Author: Philip Milne <pmilne@google.com>
Date:   Thu Jan 26 16:55:30 2012 -0800

    New hooks to allow layouts to improve their performance by doing more caching

    This change allows layouts to be notified of changes to LayoutParameters that have occurred
    between layout operations.

    If an assignment is made to the fields of LayoutParams instances that are already in use,
    cachced data may become inconsistent with the new values. For complex layouts, like
    GridLayout, in which the layout parameters define the structure of the layout, caching
    could have caused  ArrayOutOfBoundsException to be raised without this change. This case is
    rare in normal code as initialisation is typically performed once. Its nevertheless possible
    and much more likely in environments like design tools where layout parametrs may be being
    edited on the fly.

    Prevent errors as follows (belt and braces):

    1. Change javadoc to request that changes to the fields of LayoutParams be accompanied with
    a call to View.setLayoutParams(). (This calls requestLayout() which was what the previous
    javadoc advised.) Provide a (for now, private) hook for layouts with caches to receive notification
    of such calls so they can invalidate any relevant internal state.

    2. For GridLayout, we cannot clone layout parameters as traditional Java grids do without retaining
    two complete copies because of the public getLayoutParameters() method on View. Retaining two
    copies is wasteful on constrainted devices. Instead, we keep just one copy and compute a hashCode
    for the critical fields of a GridLayout's layoutParams. The hashChode is checked it prior to all
    layout operations; clearing the cache and logging a warning when changes are detected, so that
    developers can fix their code to provide the call to setLayoutParams() as above.

    Change-Id: I819ea65ec0ab82202e2f94fd5cd3ae2723c1a9a0