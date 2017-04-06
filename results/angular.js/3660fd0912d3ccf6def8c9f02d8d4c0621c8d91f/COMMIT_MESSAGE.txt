commit 3660fd0912d3ccf6def8c9f02d8d4c0621c8d91f
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Sat Aug 23 07:37:34 2014 +0100

    feat($compile/ngBind): allow disabling binding info

    The compiler and ngBind directives add binding information (`ng-binding`
    CSS class and `$binding` data property) to elements when they are bound to
    the scope. This is only to aid testing and debugging for tools such as
    Protractor and Batarang. In production this is unnecessary and add a
    performance penalty.

    This can be now disabled by calling `$compileProvider.debugInfoEnabled(false)`
    in a module `config` block:
    ```
    someModule.config(['$compileProvider', function($compileProvider) {
      $compileProvider.debugInfoEnabled(false);
    }]);
    ```

    In the bench/apps/largetable-bp benchmark this change, with debug info disabled,
    improved by ~140ms, that is 10%.
    Measuring the "create" phase, 25 loops, mean time ~1340ms -> ~1200ms.

    We were storing the whole `interpolationFn` in the `$binding` data on
    elements but this function was bringing a lot of closure variables with it
    and so was consuming unwanted amounts of memory.

    Now we are only storing the parsed interpolation expressions from the
    binding (i.e. the values of `interpolationFn.expressions`).

    BREAKING CHANGE:
    The value of `$binding` data property on an element is always an array now
    and the expressions do not include the curly braces `{{ ... }}`.