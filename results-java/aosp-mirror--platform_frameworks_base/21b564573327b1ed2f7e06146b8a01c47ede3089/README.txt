commit 21b564573327b1ed2f7e06146b8a01c47ede3089
Author: Deepanshu Gupta <deepanshu@google.com>
Date:   Tue May 19 11:55:15 2015 -0700

    Fix include tag rendering.

    A missing catch clause caused rendering to be failed when there is an
    include tag that doesn't specify layout_width and layout_height. Also
    improve the error messages to make debugging easier next time.

    Change-Id: I617762636973a010b34da167c7b5fcd328b7d178