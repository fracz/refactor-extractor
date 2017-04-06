commit cd2cfafcab0e24386a5cb9e5cc6f7a6e8d79b31b
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Sun Sep 14 18:47:37 2014 +0200

    refactor($scope): prevent multiple calls to listener on `$destroy`

    Prevent isolated scopes from having listeners that get called
    multiple times when on `$destroy`