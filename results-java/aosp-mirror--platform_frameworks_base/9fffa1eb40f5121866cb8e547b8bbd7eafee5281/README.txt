commit 9fffa1eb40f5121866cb8e547b8bbd7eafee5281
Author: Romain Guy <romainguy@android.com>
Date:   Wed Jun 24 12:42:43 2009 -0700

    RelativeLayout was ignoring some dependencies.

    This change also improves the speed of RelativeLayout by eliminating calls to
    findViewById() whenever possible.