commit c8e37ce19bf439a6a36931069fd01921ec67ae0b
Author: jwilson <jwilson@squareup.com>
Date:   Mon Jul 1 17:31:33 2013 -0400

    First step in an async HTTP API.

    This is very experimental right now but it sets us up
    for a full asynchronous API. The current implementation
    builds an async API over our existing synchronous API.
    Future refactorings to the internals should promote the
    async API throughout the codebase, particularly in SPDY.
    That way we can have more requests in flight than threads
    processing those requests.