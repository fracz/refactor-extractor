commit ed99821e4dc621864f7e2d9a6b5305fca27fb7fa
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Wed Oct 29 16:08:16 2014 +0100

    fix($parse): stateful interceptors override an `undefined` expression

    When using `$parse` with a stateful interceptor and the expression
    is `undefined`, then return the result from the interceptor

    NOTE from Igor: this is not the best solution. We need to refactor
    this and one-time + $interpolate code to properly fix this. @caitp
    is on it. See discussion in the PR.

    Closes #9821
    Closes #9825