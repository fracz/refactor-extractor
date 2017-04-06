commit e0489abd8d9e4971ae23cc38805a92d227d1f3a1
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Wed Aug 27 14:06:27 2014 -0700

    perf($compile): add debug classes in compile phase

    In a93f03d and d37f103 we changed the compiler and ngBind to add debugging CSS classes (i.e. ng-scope, ng-binding) in linking function. This simplified the code and made sense under the original assumptions that the debug info will be disabled by default. That is however not the case - debug info is enabled by default.

    When debug info is enabled, this change improves the largetable-bp
    benchmark by ~580ms, that is 30% faster.
    Measuring the “create” phase, 25 loops, meantime ~1920ms -> ~1340ms.

    This change does not affect performance when debug info is disabled.