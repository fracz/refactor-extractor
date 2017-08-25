commit 85e4af469591c77c8ba629caae34c5a87594a619
Author: Mike van Riel <me@mikevanriel.com>
Date:   Sun Jul 20 11:09:28 2014 +0200

    Re-instate the Bootstrap class

    Because Phing, and possible some other tools, use the Bootstrap
    class, which we recently broke, I have refactored the binary
    file to use the bootstrap class to initialize phpDocumentor.

    This simplifies the binary and makes it possible for phing to
    use it again.