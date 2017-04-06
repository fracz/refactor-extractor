commit a1e5cd5fe3906ebee8c400247a1f793d3e2239fb
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Sat Aug 23 07:37:34 2014 +0100

    feat($compile): allow disabling scope info

    The compiler adds scope information (`ng-scope` CSS class and `$scope` data property) to elements
    when the are bound to the scope. This is mostly to aid debugging tools such as Batarang. In
    production this should be unnecesary and adds a performance penalty.

    In the bench/apps/largetable-bp this change caused an improvement of ~100ms (7%).

    This can be now disabled by calling `$compileProvider.debugInfoEnabled(false)`
    in a module `config` block:
    ```
    someModule.config(['$compileProvider', function($compileProvider) {
      $compileProvider.debugInfoEnabled(false);
    }]);
    ```

    In the bench/apps/largetable-bp benchmark this change, with debug info disabled,
    improved by ~120ms, that is ~10%.
    Measuring the "create" phase, 25 loops, mean time ~1200ms -> ~1080ms.