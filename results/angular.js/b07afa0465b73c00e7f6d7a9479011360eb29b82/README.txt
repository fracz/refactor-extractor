commit b07afa0465b73c00e7f6d7a9479011360eb29b82
Author: Martin Probst <martinprobst@google.com>
Date:   Tue Jan 21 11:07:47 2014 +0100

    refactor(externs): move Closure Externs back to Closure code repository

    While Closure Compiler generally recommends to maintain the externs for
    projects together with their source, this has not worked well for
    AngularJS:
    - Changes to externs must be tested; they can break clients. AngularJS
      has no testing infrastructure for this.
    - Changes mostly come from users inside of Google and are much more
      easily submitted together with the code using them within Google's
      repository.

    This change deletes the externs here and adds a README.closure.md to
    document the change. They will be added back to Closure Compiler in a
    separate submit.

    Closes #5906