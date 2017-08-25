commit 65f8d9869f1f15050230a0ec0ac18b6b4e5cec4f
Author: joseph lenton <jl235@kent.ac.uk>
Date:   Sun Jun 17 07:32:06 2012 +0100

    improved class not found

    Before the class not found did not work well with multiple class
    autoloaders already in existance, now it checks for them, and
    re-arranges the class loaders so it works well.

    PHP-Error is now reported as an internal file in the stack trace.

    Also fixed a FireFox specific bug with the background height causing a
    vertical scroll bar to appear.