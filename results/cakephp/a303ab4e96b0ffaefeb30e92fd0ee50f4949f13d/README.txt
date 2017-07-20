commit a303ab4e96b0ffaefeb30e92fd0ee50f4949f13d
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Fri Jan 9 21:27:05 2015 +0100

    Implemented real non-buffered Database statements.

    The old implementation was secretly relying on PHP results buffering for
    MySQL and it was impossible to turn off.

    Also improved the Sqlite driver to only buffer results optionally as mysql
    does.