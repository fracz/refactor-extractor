commit 16685335566048af304b6e9ada81f78d3e083e38
Author: Shay Banon <kimchy@gmail.com>
Date:   Mon Jul 2 17:17:16 2012 +0200

    improve dangling index support to not detect explicit deleted index as dangling, harden when we delete the _state of an index