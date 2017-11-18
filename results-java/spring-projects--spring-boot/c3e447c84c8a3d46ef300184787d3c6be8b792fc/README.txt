commit c3e447c84c8a3d46ef300184787d3c6be8b792fc
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Mon Sep 21 15:32:53 2015 +0100

    Order char encoding filter so it sets encoding before request is read

    For the character encoding filter to work, it's vital that it sets
    the request's encoding before any other filters attempt to read the
    request. This commit updates the order of
    OrderedCharacterEncodingFilter to be HIGHEST_PRECEDENCE and improves
    the existing test to check that the ordering is as required.

    Closes gh-3912