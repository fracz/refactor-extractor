commit 1881f09ba9605f61888b79e27c5c0aef173583bb
Author: Renato Mangini <mangini@google.com>
Date:   Wed Dec 10 11:38:14 2014 -0800

    Voice actions + local UI refactor

    Added initial voice actions support (in progress);
    Refactored local UI to keep media controller state in Activity instead of in
    fragments;
    Separated local UI playback controls from PlayingQueue;

    Change-Id: I3192932fbb53a011aeef9e6284daea82a5d2bf09