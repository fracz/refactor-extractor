commit 47583d98005f6a498d397dbe2cedaadac69f0b47
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Thu Jul 14 21:59:25 2016 +0300

    fix($http): avoid `Possibly Unhandled Rejection` error when the request fails

    Calling `promise.finally(...)` creates a new promise. If we don't return this promise, the user
    won't be able to attach an error handler to it and thus won't be able to prevent a potential PUR
    error.

    This commit also improves the test coverage for the increment/decrement `outstandingRequestCount`
    fix.

    Closes #13869
    Closes #14921