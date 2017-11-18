commit 644806b2e3640ba8f730a2483b74b551524aeccd
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Mon Jun 3 23:36:57 2013 -0700

    Fix PublishSubject non-deterministic behavior on concurrent modification

    - changed to take snapshot of observers.values() before iterating in onNext/onError/onCompleted so that nested subscriptions that add to observers can't change the values() iteration
    - single-threaded nested subscriptions are now deterministic
    - multi-threaded subscriptions will no longer be allowed to race to get into an interating onNext/onError/onCompleted loop, they will always wait until the next
    - also improved terminal state behavior when subscribing to a PublishSubject that has already received onError/onCompleted

    https://github.com/Netflix/RxJava/issues/282