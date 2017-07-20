commit c6c1bf110dd9df6ce01e6a220e3f77262a73818f
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Fri Jul 15 20:44:39 2011 -0430

    Fixing caching of class loading in App class, this was broken after a recent refactoring
    Additionally a new property $bootstrapping is added to App, this is set during the bootstrap process to indicate that classes loaded before the caching is initialized should not trigger the cache write routine.
    Performance++