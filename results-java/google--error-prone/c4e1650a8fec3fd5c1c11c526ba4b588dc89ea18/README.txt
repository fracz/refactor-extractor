commit c4e1650a8fec3fd5c1c11c526ba4b588dc89ea18
Author: Caitlin Sadowski <supertri@google.com>
Date:   Tue Oct 9 16:58:12 2012 -0700

    Working version of the ReturnValueIgnored check.

    Check now handles various tricky cases involving calling methods on
    "this". Still could use a little refactoring.

    Paired with eaftan.