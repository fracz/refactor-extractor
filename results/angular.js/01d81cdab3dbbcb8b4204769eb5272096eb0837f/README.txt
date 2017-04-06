commit 01d81cdab3dbbcb8b4204769eb5272096eb0837f
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Fri Aug 8 14:10:03 2014 +0100

    fix(jqLite): allow `triggerHandler()` to accept custom event

    In some scenarios you want to be able to specify properties on the event
    that is passed to the event handler. JQuery does this by overloading the
    first parameter (`eventName`). If it is an object with a `type` property
    then we assume that it must be a custom event.

    In this case the custom event must provide the `type` property which is
    the name of the event to be triggered.  `triggerHandler` will continue to
    provide dummy default functions for `preventDefault()`, `isDefaultPrevented()`
    and `stopPropagation()` but you may override these with your own versions
    in your custom object if you wish.

    In addition the commit provides some performance and memory usage
    improvements by only creating objects and doing work that is necessary.

    This commit also renames the parameters inline with jQuery.

    Closes #8469