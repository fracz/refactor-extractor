commit 2288129d5208cd26ab41191db69a418d15ead9ee
Author: Felipe Leme <felipeal@google.com>
Date:   Wed Jan 6 09:57:23 2016 -0800

    Fixed corner-case scenario where a screenshot is finished after the share
    notification is sent.

    Prior to this change, if a screenshot finished after the share
    notification was sent, it would replace the share notification with a
    progress notification, and the share notification would never be sent
    again.

    Also improved the test cases that automatically generate a screenshot
    but don't use it to wait for the screenshot to finish before proceeding,
    otherwise it could cause a future test to fail (if the screenshot is
    finished after the initial test is completed).

    Change-Id: I6e2a6549ebb48e5bebf5aa78d1bda94404c1812b