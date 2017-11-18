commit 0412b317acfb64c9d78edcdfd3a92634e9b62b80
Author: Deepanshu Gupta <deepanshu@google.com>
Date:   Wed Mar 11 16:48:20 2015 -0700

    Tools attribute improvements for RecyclerView LayoutManager.

    1. Change tools attribute for layout manager from layoutManagerType to
    LayoutManager.
    2. Support classnames in the tools attribute.

    This change doesn't try to supprt arbitrary classnames for now. The
    reason for this is that the new layoutlib API is supposed to contain a
    new method for inflating custom classes that are not views. This will
    allow us to throw better error messages. Without the new API, trying for
    different constructors, will result in a error messages being logged.

    Change-Id: I3a31359c06b7452bfd973c3e5e54f9038acccfaa