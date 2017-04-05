commit 57e6e0881deee32ac9ebe2a01ae85d9a8233c7f3
Author: Sam Brannen <sam@sambrannen.com>
Date:   Tue Dec 4 01:03:20 2012 +0100

    Introduce isExecuted() in MockClientHttpRequest

    This commit introduces the missing isExecuted() method in
    MockClientHttpRequest and improves the documentation for execute()
    and executeInternal().