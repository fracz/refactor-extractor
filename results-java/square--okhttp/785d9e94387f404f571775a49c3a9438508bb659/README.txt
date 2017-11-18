commit 785d9e94387f404f571775a49c3a9438508bb659
Author: Jesse Wilson <jwilson@squareup.com>
Date:   Sat Dec 27 13:05:33 2014 -0500

    Use MockWebServerRule in more tests.

    Also fix a bug in MockWebServer where calls to shutdown()
    raced with calls to play() would throw a NullPointerException.

    Also improve logging in MockWebServer.