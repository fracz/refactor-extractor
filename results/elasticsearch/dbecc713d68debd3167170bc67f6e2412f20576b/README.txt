commit dbecc713d68debd3167170bc67f6e2412f20576b
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Jan 9 12:17:02 2014 +0100

    improve buffered output
    - use the same buffer size if wrapping a buffered output
    - directly call flush on the checksum wrapper