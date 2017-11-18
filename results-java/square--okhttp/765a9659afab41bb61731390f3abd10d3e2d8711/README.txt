commit 765a9659afab41bb61731390f3abd10d3e2d8711
Author: Neil Fuller <nfuller@google.com>
Date:   Thu Aug 6 17:23:12 2015 +0100

    Test code refactoring

    Modify the various Delegating*SocketFactory implementations
    to return a *Socket from configureSocket() rather than assume
    configuration just modifies the object supplied. Improves
    flexibility and reduces the need to override multiple methods
    in some places.