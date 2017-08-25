commit a0670b9f0c499bff6deb33da558754c8bf0265f6
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Mon Nov 4 11:19:35 2013 -0500

    Respect skips in setup section

    Due to the nature of the Symfony yaml parser, new documents in the
    same stream ("---") must be manually extracted. This setup meant
    that the yaml runner was not applying skips in the setup to the
    entire document stream.

    Implemented a fairly hacky way to fix it, will need refactoring
    at some point (once the whole runner gets a facelift)