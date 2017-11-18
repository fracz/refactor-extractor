commit ffcf88f36ec8412bc274a51cad3bf03c11c614fb
Author: Marie Janssen <jamuraa@google.com>
Date:   Tue Dec 13 10:51:02 2016 -0800

    Bluetooth: log message improvements

    Some log improvements:
     - Reduce logspam
     - Use names for states in logs instead of numbers
     - Be more consistent with messages

    Also remove some commented out dead code.

    Test: run on phone, observe more useful logs
    Change-Id: I32163278e148be144c03d4e8aaf0eb761226c94c