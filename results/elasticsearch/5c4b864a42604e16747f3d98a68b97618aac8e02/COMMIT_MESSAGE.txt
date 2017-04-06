commit 5c4b864a42604e16747f3d98a68b97618aac8e02
Author: Jason Tedor <jason@tedor.me>
Date:   Thu Sep 3 13:01:17 2015 -0400

    Workaround pitfall in Java 8 target-type inference

    Target-type inference has been improved in Java 8. This leads to these
    lines now being interpreted as invoking String#valueOf(char[]) whereas
    they previously were interpreted as invoking String#valueOf(Object).
    This change leads to ClassCastExceptions during test execution. Simply
    casting the parameter to Object restores the old invocation.

    Closes #13315