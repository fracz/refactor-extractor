commit 9eb049791037ecb65da18b562ff57804fbd6e942
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Tue Jan 22 15:41:28 2013 -0800

    Performance refactoring: OperatorSubscribeFunction

    - migrated Func1 to OperatorSubscribeFunction for internal operator implementations
    - do not wrap with AtomicObserver when it's a trusted operator

    https://github.com/Netflix/RxJava/issues/104