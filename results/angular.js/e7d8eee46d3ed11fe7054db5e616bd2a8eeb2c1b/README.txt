commit e7d8eee46d3ed11fe7054db5e616bd2a8eeb2c1b
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Thu Dec 15 15:04:50 2016 +0200

    refactor(ngAnimate): simplify functions and remove redundant args/calls

    Simplifies/Optimizes the following functions:

    - `areAnimationsAllowed()`
    - `cleanupEventListeners()`
    - `closeChildAnimations()`
    - `clearElementAnimationState()`
    - `markElementAnimationState()`
    - `findCallbacks()`

    Although not its primary aim, this commit also offers a small performance boost
    to animations (~5% as measured with the `animation-bp` benchmark).