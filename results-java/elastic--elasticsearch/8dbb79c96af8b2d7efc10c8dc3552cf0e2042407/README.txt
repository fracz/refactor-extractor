commit 8dbb79c96af8b2d7efc10c8dc3552cf0e2042407
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Apr 21 10:20:34 2015 +0200

    refactor and cleanup transport request handling

    This refactoring and cleanup is that each request handler ends up
    implementing too many methods that can be provided when the request handler itself
    is registered, including a prototype like class that can be used to instantiate
    new request instances for streaming.
    closes #10730