commit ee05681139f2eb5a9994d7294441d694a309407f
Author: Andrii Kulian <akulian@google.com>
Date:   Wed Sep 21 15:34:45 2016 -0700

    Fix stopping activity when removed from waiting visible

    During refactoring in ag/613666 waitingVisible flag clearing
    was mistakenly removed. It was added back in ag/1221328 but
    only for activities that are finishing.
    This caused activity not being stopped in some cases when
    window visibility change was handled before receiving idle
    callback from client. This is easily reproducible when there
    is a scene transition animation specified.

    Bug: 31078584
    Change-Id: Ic09c5199ad4fceae0607e4bcce02be5335c8870b